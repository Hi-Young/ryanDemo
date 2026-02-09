package com.ryan.deadlock.aspect;

import com.ryan.deadlock.monitor.DeadlockMonitor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 死锁监控切面
 * 
 * 功能：
 * 1. 自动捕获方法执行中的死锁异常
 * 2. 记录死锁发生的详细信息
 * 3. 触发死锁分析和报告
 * 4. 提供死锁重试机制
 */
@Slf4j
@Aspect
@Component
public class DeadlockMonitorAspect {
    
    @Autowired
    private DeadlockMonitor deadlockMonitor;
    
    private final AtomicLong deadlockExceptionCount = new AtomicLong(0);
    
    /**
     * 死锁监控注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MonitorDeadlock {
        String value() default ""; // 操作描述
        boolean autoRetry() default false; // 是否自动重试
        int maxRetries() default 3; // 最大重试次数
        long retryDelay() default 100; // 重试延迟(毫秒)
        boolean enableReport() default true; // 是否生成报告
    }
    
    /**
     * 死锁监控环绕通知
     */
    @Around("@annotation(monitorDeadlock)")
    public Object monitorDeadlock(ProceedingJoinPoint joinPoint, MonitorDeadlock monitorDeadlock) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operation = monitorDeadlock.value().isEmpty() ? methodName : monitorDeadlock.value();
        
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();
        
        int retryCount = 0;
        int maxRetries = monitorDeadlock.autoRetry() ? monitorDeadlock.maxRetries() : 0;
        
        while (true) {
            try {
                log.info("🔍 开始监控死锁 - {}#{} [{}] (第{}次尝试)", 
                    className, methodName, operation, retryCount + 1);
                
                Object result = joinPoint.proceed(args);
                
                long executionTime = System.currentTimeMillis() - startTime;
                log.info("✅ 方法执行成功 - {}#{} [{}] 执行时间: {}ms", 
                    className, methodName, operation, executionTime);
                
                return result;
                
            } catch (DeadlockLoserDataAccessException deadlockEx) {
                // 捕获Spring的死锁异常
                handleDeadlockException(deadlockEx, className, methodName, operation, 
                    retryCount, maxRetries, monitorDeadlock);
                
                if (retryCount >= maxRetries) {
                    throw deadlockEx;
                }
                
                // 重试前等待
                if (monitorDeadlock.retryDelay() > 0) {
                    Thread.sleep(monitorDeadlock.retryDelay());
                }
                retryCount++;
                
            } catch (SQLException sqlEx) {
                // 捕获原生SQL死锁异常
                if (isDeadlockException(sqlEx)) {
                    handleDeadlockException(sqlEx, className, methodName, operation, 
                        retryCount, maxRetries, monitorDeadlock);
                    
                    if (retryCount >= maxRetries) {
                        throw sqlEx;
                    }
                    
                    if (monitorDeadlock.retryDelay() > 0) {
                        Thread.sleep(monitorDeadlock.retryDelay());
                    }
                    retryCount++;
                } else {
                    throw sqlEx;
                }
                
            } catch (Exception ex) {
                // 检查是否是由死锁引起的其他异常
                if (isDeadlockCausedException(ex)) {
                    handleDeadlockException(ex, className, methodName, operation, 
                        retryCount, maxRetries, monitorDeadlock);
                    
                    if (retryCount >= maxRetries) {
                        throw ex;
                    }
                    
                    if (monitorDeadlock.retryDelay() > 0) {
                        Thread.sleep(monitorDeadlock.retryDelay());
                    }
                    retryCount++;
                } else {
                    throw ex;
                }
            }
        }
    }
    
    /**
     * 处理死锁异常
     */
    private void handleDeadlockException(Exception ex, String className, String methodName, 
                                       String operation, int retryCount, int maxRetries, 
                                       MonitorDeadlock monitorDeadlock) {
        
        long count = deadlockExceptionCount.incrementAndGet();
        
        log.error("💀 检测到死锁异常 (第{}次) - {}#{} [{}]", 
            count, className, methodName, operation);
        log.error("异常信息: {}", ex.getMessage());
        
        // 生成死锁报告
        if (monitorDeadlock.enableReport()) {
            try {
                DeadlockMonitor.DeadlockInfo deadlockInfo = deadlockMonitor.checkAndReportDeadlock();
                if (deadlockInfo != null) {
                    log.error("📊 死锁详细分析: \\n{}", deadlockInfo.getAnalysis());
                } else {
                    log.warn("⚠️ 无法获取死锁详细信息，可能死锁已被自动解决");
                }
            } catch (Exception reportEx) {
                log.error("生成死锁报告失败: {}", reportEx.getMessage());
            }
        }
        
        // 记录重试信息
        if (retryCount < maxRetries) {
            log.warn("🔄 准备进行第{}次重试 (最多{}次)", retryCount + 2, maxRetries + 1);
        } else {
            log.error("❌ 达到最大重试次数，放弃执行 - {}#{} [{}]", 
                className, methodName, operation);
        }
    }
    
    /**
     * 判断是否是死锁异常
     */
    private boolean isDeadlockException(SQLException ex) {
        // MySQL死锁错误码: 1213
        return ex.getErrorCode() == 1213 || 
               ex.getMessage().toLowerCase().contains("deadlock") ||
               ex.getMessage().toLowerCase().contains("try restarting transaction");
    }
    
    /**
     * 判断是否是由死锁引起的异常
     */
    private boolean isDeadlockCausedException(Exception ex) {
        // 检查异常消息
        String message = ex.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("deadlock") || 
                lowerMessage.contains("lock wait timeout") ||
                lowerMessage.contains("try restarting transaction")) {
                return true;
            }
        }
        
        // 检查原因异常
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof SQLException) {
                return isDeadlockException((SQLException) cause);
            }
            if (cause instanceof DeadlockLoserDataAccessException) {
                return true;
            }
            
            String causeMessage = cause.getMessage();
            if (causeMessage != null && 
                causeMessage.toLowerCase().contains("deadlock")) {
                return true;
            }
            
            cause = cause.getCause();
        }
        
        return false;
    }
    
    /**
     * 获取死锁异常统计
     */
    public long getDeadlockExceptionCount() {
        return deadlockExceptionCount.get();
    }
    
    /**
     * 重置死锁异常计数
     */
    public void resetDeadlockExceptionCount() {
        deadlockExceptionCount.set(0);
        log.info("死锁异常计数器已重置");
    }
    
    /**
     * 通用的死锁监控 - 监控所有service层方法
     */
    @Around("execution(* com.ryan.deadlock.service.*.*(..))")
    public Object monitorServiceDeadlock(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            if (isDeadlockCausedException(ex) || ex instanceof DeadlockLoserDataAccessException) {
                long count = deadlockExceptionCount.incrementAndGet();
                log.error("💀 Service层死锁异常 (第{}次) - {}#{}", count, className, methodName);
                log.error("异常详情: {}", ex.getMessage());
                
                // 触发死锁分析
                try {
                    deadlockMonitor.checkAndReportDeadlock();
                } catch (Exception reportEx) {
                    log.error("死锁分析失败: {}", reportEx.getMessage());
                }
            }
            throw ex;
        }
    }
}