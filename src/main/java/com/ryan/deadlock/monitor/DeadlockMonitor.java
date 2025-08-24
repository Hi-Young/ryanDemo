package com.ryan.deadlock.monitor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库死锁监控工具
 * 
 * 功能：
 * 1. 实时监控InnoDB状态中的死锁信息
 * 2. 解析死锁日志，提取关键信息
 * 3. 统计死锁发生频率和类型
 * 4. 提供死锁预警和分析报告
 */
@Slf4j
@Component
public class DeadlockMonitor {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 死锁统计计数器
    private final AtomicLong deadlockCount = new AtomicLong(0);
    private final Map<String, AtomicLong> deadlockTypeCount = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 死锁信息数据结构
     */
    @Data
    public static class DeadlockInfo {
        private LocalDateTime timestamp;
        private String deadlockType;
        private String transaction1Info;
        private String transaction2Info; 
        private String waitingFor;
        private String holdsLock;
        private String victimTransaction;
        private String sqlStatements;
        private String analysis;
    }
    
    /**
     * 获取当前InnoDB状态信息
     */
    public String getInnodbStatus() {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SHOW ENGINE INNODB STATUS");
            if (!result.isEmpty()) {
                return (String) result.get(0).get("Status");
            }
        } catch (Exception e) {
            log.error("获取InnoDB状态失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 解析死锁信息
     */
    public DeadlockInfo parseDeadlockInfo(String innodbStatus) {
        if (innodbStatus == null || !innodbStatus.contains("LATEST DETECTED DEADLOCK")) {
            return null;
        }
        
        DeadlockInfo deadlockInfo = new DeadlockInfo();
        
        try {
            // 提取时间戳
            Pattern timePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");
            Matcher timeMatcher = timePattern.matcher(innodbStatus);
            if (timeMatcher.find()) {
                deadlockInfo.setTimestamp(LocalDateTime.parse(timeMatcher.group(1), formatter));
            }
            
            // 提取死锁部分
            String deadlockSection = extractDeadlockSection(innodbStatus);
            if (deadlockSection != null) {
                // 分析死锁类型
                deadlockInfo.setDeadlockType(analyzeDeadlockType(deadlockSection));
                
                // 提取事务信息
                deadlockInfo.setTransaction1Info(extractTransactionInfo(deadlockSection, "TRANSACTION 1"));
                deadlockInfo.setTransaction2Info(extractTransactionInfo(deadlockSection, "TRANSACTION 2"));
                
                // 提取等待和持有的锁信息
                deadlockInfo.setWaitingFor(extractWaitingFor(deadlockSection));
                deadlockInfo.setHoldsLock(extractHoldsLock(deadlockSection));
                
                // 提取被选为牺牲品的事务
                deadlockInfo.setVictimTransaction(extractVictimTransaction(deadlockSection));
                
                // 提取SQL语句
                deadlockInfo.setSqlStatements(extractSqlStatements(deadlockSection));
                
                // 生成分析建议
                deadlockInfo.setAnalysis(generateAnalysis(deadlockInfo));
                
                // 更新统计计数
                updateStatistics(deadlockInfo.getDeadlockType());
            }
            
        } catch (Exception e) {
            log.error("解析死锁信息失败: {}", e.getMessage());
        }
        
        return deadlockInfo;
    }
    
    /**
     * 提取死锁部分内容
     */
    private String extractDeadlockSection(String innodbStatus) {
        int startIdx = innodbStatus.indexOf("LATEST DETECTED DEADLOCK");
        if (startIdx == -1) return null;
        
        int endIdx = innodbStatus.indexOf("WE ROLL BACK TRANSACTION", startIdx);
        if (endIdx == -1) {
            endIdx = innodbStatus.indexOf("--------", startIdx + 100);
        }
        
        if (endIdx > startIdx) {
            return innodbStatus.substring(startIdx, endIdx);
        }
        return null;
    }
    
    /**
     * 分析死锁类型
     */
    private String analyzeDeadlockType(String deadlockSection) {
        if (deadlockSection.contains("lock_mode X locks rec but not gap")) {
            return "记录锁死锁";
        } else if (deadlockSection.contains("lock_mode X locks gap")) {
            return "Gap锁死锁";  
        } else if (deadlockSection.contains("lock_mode X")) {
            return "排他锁死锁";
        } else if (deadlockSection.contains("lock_mode S")) {
            return "共享锁死锁";
        } else if (deadlockSection.contains("insert intention")) {
            return "插入意向锁死锁";
        } else {
            return "未知类型死锁";
        }
    }
    
    /**
     * 提取事务信息
     */
    private String extractTransactionInfo(String deadlockSection, String transactionMarker) {
        int startIdx = deadlockSection.indexOf(transactionMarker);
        if (startIdx == -1) return null;
        
        int endIdx = deadlockSection.indexOf("TRANSACTION", startIdx + transactionMarker.length());
        if (endIdx == -1) {
            endIdx = deadlockSection.indexOf("*** WE ROLL BACK", startIdx);
        }
        
        if (endIdx > startIdx) {
            return deadlockSection.substring(startIdx, endIdx).trim();
        }
        return null;
    }
    
    /**
     * 提取等待锁信息
     */
    private String extractWaitingFor(String deadlockSection) {
        Pattern pattern = Pattern.compile("WAITING FOR this lock to be granted:[\\s\\S]*?(?=HOLDS THE LOCK|WE ROLL BACK)");
        Matcher matcher = pattern.matcher(deadlockSection);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }
    
    /**
     * 提取持有锁信息
     */
    private String extractHoldsLock(String deadlockSection) {
        Pattern pattern = Pattern.compile("HOLDS THE LOCK\\(S\\):[\\s\\S]*?(?=WAITING FOR|TRANSACTION|WE ROLL BACK)");
        Matcher matcher = pattern.matcher(deadlockSection);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }
    
    /**
     * 提取被回滚的事务
     */
    private String extractVictimTransaction(String deadlockSection) {
        Pattern pattern = Pattern.compile("WE ROLL BACK TRANSACTION \\((\\d+)\\)");
        Matcher matcher = pattern.matcher(deadlockSection);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 提取SQL语句
     */
    private String extractSqlStatements(String deadlockSection) {
        StringBuilder sqlStatements = new StringBuilder();
        Pattern pattern = Pattern.compile("MySQL thread id \\d+.*?(?=MySQL thread id|WE ROLL BACK|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(deadlockSection);
        
        while (matcher.find()) {
            String threadInfo = matcher.group();
            if (threadInfo.contains("query")) {
                sqlStatements.append(threadInfo).append("\\n\\n");
            }
        }
        
        return sqlStatements.toString();
    }
    
    /**
     * 生成分析建议
     */
    private String generateAnalysis(DeadlockInfo deadlockInfo) {
        StringBuilder analysis = new StringBuilder();
        
        String deadlockType = deadlockInfo.getDeadlockType();
        
        analysis.append("【死锁分析】\\n");
        analysis.append("死锁类型: ").append(deadlockType).append("\\n");
        
        switch (deadlockType) {
            case "记录锁死锁":
                analysis.append("建议: \\n");
                analysis.append("1. 统一事务中的加锁顺序，避免循环等待\\n");
                analysis.append("2. 缩短事务执行时间，减少锁持有时间\\n");
                analysis.append("3. 考虑使用乐观锁替代悲观锁\\n");
                break;
                
            case "Gap锁死锁":
                analysis.append("建议: \\n");
                analysis.append("1. 降低事务隔离级别到READ_COMMITTED\\n");
                analysis.append("2. 避免大范围的区间查询\\n");
                analysis.append("3. 使用唯一索引约束避免重复插入\\n");
                break;
                
            case "插入意向锁死锁":
                analysis.append("建议: \\n");
                analysis.append("1. 批量插入时先排序，避免Gap锁冲突\\n");
                analysis.append("2. 使用INSERT ... ON DUPLICATE KEY UPDATE\\n");
                analysis.append("3. 预先创建足够的主键值空间\\n");
                break;
                
            default:
                analysis.append("建议: \\n");
                analysis.append("1. 分析具体的锁等待情况\\n");
                analysis.append("2. 优化SQL执行计划\\n");
                analysis.append("3. 考虑业务层面的优化\\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * 更新死锁统计
     */
    private void updateStatistics(String deadlockType) {
        deadlockCount.incrementAndGet();
        deadlockTypeCount.computeIfAbsent(deadlockType, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * 检查并报告死锁
     */
    public DeadlockInfo checkAndReportDeadlock() {
        String innodbStatus = getInnodbStatus();
        if (innodbStatus == null) {
            return null;
        }
        
        DeadlockInfo deadlockInfo = parseDeadlockInfo(innodbStatus);
        if (deadlockInfo != null) {
            log.warn("🚨 检测到数据库死锁！");
            log.warn("时间: {}", deadlockInfo.getTimestamp());
            log.warn("类型: {}", deadlockInfo.getDeadlockType());
            log.warn("牺牲事务: {}", deadlockInfo.getVictimTransaction());
            log.warn("分析建议: \\n{}", deadlockInfo.getAnalysis());
        }
        
        return deadlockInfo;
    }
    
    /**
     * 获取死锁统计信息
     */
    public String getDeadlockStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== 死锁统计报告 ===\\n");
        stats.append("总死锁次数: ").append(deadlockCount.get()).append("\\n");
        stats.append("\\n各类型死锁统计:\\n");
        
        deadlockTypeCount.forEach((type, count) -> {
            stats.append("- ").append(type).append(": ").append(count.get()).append("次\\n");
        });
        
        return stats.toString();
    }
    
    /**
     * 重置统计计数器
     */
    public void resetStatistics() {
        deadlockCount.set(0);
        deadlockTypeCount.clear();
        log.info("死锁统计计数器已重置");
    }
    
    /**
     * 获取当前锁等待信息
     */
    public List<Map<String, Object>> getCurrentLockWaits() {
        try {
            String sql = "SELECT " +
                    "r.trx_id waiting_trx_id, " +
                    "r.trx_mysql_thread_id waiting_thread, " +
                    "r.trx_query waiting_query, " +
                    "b.trx_id blocking_trx_id, " +
                    "b.trx_mysql_thread_id blocking_thread, " +
                    "b.trx_query blocking_query " +
                    "FROM information_schema.innodb_lock_waits w " +
                    "INNER JOIN information_schema.innodb_trx b ON b.trx_id = w.blocking_trx_id " +
                    "INNER JOIN information_schema.innodb_trx r ON r.trx_id = w.requesting_trx_id";
            
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("获取锁等待信息失败: {}", e.getMessage());
            return null;
        }
    }
}