package com.ryan.deadlock.service;

import com.ryan.deadlock.mapper.AccountMapper;
import com.ryan.deadlock.mapper.OrderLockMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Lock-wait -> long transaction -> cascading slow transactions.
 *
 * <p>Scenario (MySQL 5.7/InnoDB):
 * <pre>
 * Tx-B holds row lock on order_locks(order_no=xxx) for a while
 * Tx-A locks account(ACC001) first, then tries to lock the same order row -> waits on Tx-B
 * Tx-C tries to lock/update account(ACC001) -> waits on Tx-A
 * </pre>
 *
 * This is a common "DB lock wait makes Spring @Transactional look like a long transaction" pattern.
 */
@Slf4j
@Service
public class LockWaitChainService {

    private final OrderLockMapper orderLockMapper;
    private final AccountMapper accountMapper;
    private final JdbcTemplate jdbcTemplate;

    public LockWaitChainService(OrderLockMapper orderLockMapper, AccountMapper accountMapper, JdbcTemplate jdbcTemplate) {
        this.orderLockMapper = orderLockMapper;
        this.accountMapper = accountMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Tx-B: Hold a row lock on order_locks for some time.
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void holdOrderLock(String orderNo, long holdMs) {
        long ms = clamp(holdMs, 100, 300_000);
        long mysqlConnId = currentMysqlConnectionId();
        log.warn("[Tx-B] mysqlConnId={}, lock order row and hold {}ms, orderNo={}", mysqlConnId, ms, orderNo);
        orderLockMapper.selectByOrderNoForUpdate(orderNo);
        sleepQuietly(ms);
        // Touch the row so it's easy to spot in binlog/audit (optional).
        orderLockMapper.updateOrderStatus(orderNo, 1);
        log.warn("[Tx-B] mysqlConnId={}, releasing order row lock, orderNo={}", mysqlConnId, orderNo);
    }

    /**
     * Tx-A: Lock account first, then wait for the same order row held by Tx-B.
     * While waiting, Tx-A keeps holding the account lock, causing other transactions to queue behind it.
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void chainTransactionA(String accountNo, String orderNo) {
        long mysqlConnId = currentMysqlConnectionId();
        log.warn("[Tx-A] mysqlConnId={}, lock account first, accountNo={}, then lock orderNo={} (may block)",
                mysqlConnId, accountNo, orderNo);
        accountMapper.selectByAccountNoForUpdate(accountNo);

        // This call will block if Tx-B is holding the order row lock.
        orderLockMapper.selectByOrderNoForUpdate(orderNo);

        // If we got here, Tx-B released the lock. Touch the account so we can see commit ordering.
        accountMapper.updateBalanceByAccountNo(accountNo, new BigDecimal("0.01"));
        log.warn("[Tx-A] mysqlConnId={}, acquired order lock and updated account, accountNo={}, orderNo={}",
                mysqlConnId, accountNo, orderNo);
    }

    /**
     * Tx-C: Contend on the same account row locked by Tx-A.
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void touchAccount(String accountNo) {
        long mysqlConnId = currentMysqlConnectionId();
        log.warn("[Tx-C] mysqlConnId={}, lock/update account (may block), accountNo={}", mysqlConnId, accountNo);
        accountMapper.selectByAccountNoForUpdate(accountNo);
        accountMapper.updateBalanceByAccountNo(accountNo, new BigDecimal("0.01"));
        log.warn("[Tx-C] mysqlConnId={}, updated account, accountNo={}", mysqlConnId, accountNo);
    }

    private long currentMysqlConnectionId() {
        // JdbcTemplate will reuse the transactional connection inside @Transactional methods,
        // so this ID can be correlated with information_schema.innodb_trx.trx_mysql_thread_id.
        Long id = jdbcTemplate.queryForObject("SELECT CONNECTION_ID()", Long.class);
        return id == null ? -1L : id;
    }

    private static void sleepQuietly(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(max, v));
    }
}
