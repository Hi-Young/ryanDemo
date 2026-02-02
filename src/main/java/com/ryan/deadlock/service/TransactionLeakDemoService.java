package com.ryan.deadlock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo: manual transaction + "forgot commit/close" -> long-lived row lock + connection leak.
 *
 * <p>Why this can "snowball":
 * <ul>
 *   <li>The leaked connection keeps an uncommitted transaction open, holding row locks (InnoDB).</li>
 *   <li>Other requests that need the same row lock will block and occupy request threads.</li>
 *   <li>Meanwhile the leaked connections are not returned to the pool, causing pool exhaustion.</li>
 * </ul>
 */
@Slf4j
@Service
public class TransactionLeakDemoService {

    private static final int MAX_LEAKS = 64;

    private final DataSource dataSource;
    private final ConcurrentHashMap<String, LeakedTx> leaks = new ConcurrentHashMap<>();

    public TransactionLeakDemoService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> leakConnectionOnly() {
        String leakId = newLeakId();
        if (leaks.size() >= MAX_LEAKS) {
            throw new IllegalStateException("too many leaked tx; call /close-all first");
        }

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            long mysqlConnectionId = queryConnectionId(conn);

            // Touch the DB so the connection is fully "active" and easy to identify in processlist.
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                // no-op
            }

            LeakedTx leakedTx = new LeakedTx(leakId, mysqlConnectionId, conn, "CONNECTION_ONLY", null,
                    Instant.now(), Thread.currentThread().getName());
            leaks.put(leakId, leakedTx);
            log.error("[TX-LEAK] leaked connection created: leakId={}, mysqlConnId={}, thread={}",
                    leakId, leakedTx.mysqlConnectionId, leakedTx.threadName);
            return leakedTx.toMap();
        } catch (SQLException e) {
            closeQuietly(conn);
            throw new IllegalStateException("failed to create leaked connection", e);
        }
    }

    /**
     * Lock one account row (FOR UPDATE) and do an UPDATE without commit/close.
     * This simulates: manual transaction management + forgot commit/rollback/close.
     */
    public Map<String, Object> leakAccountRowLock(String accountNo, BigDecimal delta) {
        String leakId = newLeakId();
        if (leaks.size() >= MAX_LEAKS) {
            throw new IllegalStateException("too many leaked tx; call /close-all first");
        }
        if (accountNo == null || accountNo.trim().isEmpty()) {
            throw new IllegalArgumentException("accountNo is required");
        }
        if (delta == null) {
            delta = new BigDecimal("0.01");
        }

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            long mysqlConnectionId = queryConnectionId(conn);

            Integer id = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM account WHERE account_no = ? FOR UPDATE")) {
                ps.setString(1, accountNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }
            }
            if (id == null) {
                // No such row; do not leak.
                conn.rollback();
                closeQuietly(conn);
                throw new IllegalArgumentException("account not found: " + accountNo);
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE account SET balance = balance + ?, update_time = NOW() WHERE account_no = ?")) {
                ps.setBigDecimal(1, delta);
                ps.setString(2, accountNo);
                ps.executeUpdate();
            }

            LeakedTx leakedTx = new LeakedTx(leakId, mysqlConnectionId, conn, "ACCOUNT_ROW_LOCK",
                    "account_no=" + accountNo, Instant.now(), Thread.currentThread().getName());
            leaks.put(leakId, leakedTx);

            log.error("[TX-LEAK] leaked tx created (FOR UPDATE + UPDATE, no commit/close): leakId={}, mysqlConnId={}, locked={}, thread={}",
                    leakId, leakedTx.mysqlConnectionId, leakedTx.lockedResource, leakedTx.threadName);
            return leakedTx.toMap();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            closeQuietly(conn);
            throw new IllegalStateException("failed to create leaked tx", e);
        }
    }

    public List<Map<String, Object>> listLeaks() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (LeakedTx tx : leaks.values()) {
            out.add(tx.toMap());
        }
        return out;
    }

    public Map<String, Object> commitAndClose(String leakId) {
        LeakedTx tx = removeLeak(leakId);
        synchronized (tx) {
            try {
                tx.connection.commit();
            } catch (SQLException e) {
                throw new IllegalStateException("commit failed: " + leakId, e);
            } finally {
                closeQuietly(tx.connection);
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", "commitAndClose");
        m.put("leakId", leakId);
        return m;
    }

    public Map<String, Object> rollbackAndClose(String leakId) {
        LeakedTx tx = removeLeak(leakId);
        synchronized (tx) {
            try {
                tx.connection.rollback();
            } catch (SQLException e) {
                throw new IllegalStateException("rollback failed: " + leakId, e);
            } finally {
                closeQuietly(tx.connection);
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", "rollbackAndClose");
        m.put("leakId", leakId);
        return m;
    }

    public Map<String, Object> closeAll() {
        int closed = 0;
        List<String> ids = new ArrayList<>(leaks.keySet());
        for (String id : ids) {
            LeakedTx tx = leaks.remove(id);
            if (tx == null) {
                continue;
            }
            synchronized (tx) {
                rollbackQuietly(tx.connection);
                closeQuietly(tx.connection);
                closed++;
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", "closeAll");
        m.put("closed", closed);
        return m;
    }

    @PreDestroy
    public void onShutdown() {
        // Best-effort cleanup to avoid leaving DB locks after stopping the app.
        try {
            closeAll();
        } catch (Exception ignore) {
            // no-op
        }
    }

    private LeakedTx removeLeak(String leakId) {
        if (leakId == null || leakId.trim().isEmpty()) {
            throw new IllegalArgumentException("leakId is required");
        }
        LeakedTx tx = leaks.remove(leakId);
        if (tx == null) {
            throw new IllegalArgumentException("leaked tx not found: " + leakId);
        }
        return tx;
    }

    private static String newLeakId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignore) {
            // no-op
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ignore) {
            // no-op
        }
    }

    private static long queryConnectionId(Connection conn) throws SQLException {
        // Use MySQL's connection id so we can correlate with information_schema.innodb_trx.trx_mysql_thread_id.
        try (PreparedStatement ps = conn.prepareStatement("SELECT CONNECTION_ID()");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return -1L;
    }

    private static final class LeakedTx {
        private final String leakId;
        private final long mysqlConnectionId;
        private final Connection connection;
        private final String type;
        private final String lockedResource;
        private final Instant createdAt;
        private final String threadName;

        private LeakedTx(String leakId, long mysqlConnectionId, Connection connection, String type,
                         String lockedResource, Instant createdAt, String threadName) {
            this.leakId = leakId;
            this.mysqlConnectionId = mysqlConnectionId;
            this.connection = connection;
            this.type = type;
            this.lockedResource = lockedResource;
            this.createdAt = createdAt;
            this.threadName = threadName;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("leakId", leakId);
            m.put("mysqlConnectionId", mysqlConnectionId);
            m.put("type", type);
            m.put("lockedResource", lockedResource);
            m.put("createdAt", createdAt.toString());
            m.put("threadName", threadName);
            return m;
        }
    }
}
