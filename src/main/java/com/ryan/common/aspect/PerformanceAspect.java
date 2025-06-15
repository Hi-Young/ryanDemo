package com.ryan.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 性能监控切面 - 监控方法执行时间
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    /**
     * 自定义注解：标记需要监控性能的方法
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MonitorPerformance {
        String value() default ""; // 操作描述
        long threshold() default 1000; // 慢查询阈值(毫秒)
    }

    /**
     * 环绕通知：监控方法执行时间
     */
    @Around("@annotation(monitorPerformance)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint, MonitorPerformance monitorPerformance) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operation = monitorPerformance.value().isEmpty() ? methodName : monitorPerformance.value();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // 正常执行日志
            if (executionTime > monitorPerformance.threshold()) {
                log.warn("⚠️ 慢方法警告 - {}#{} [{}] 执行时间: {}ms (超过阈值{}ms)", 
                    className, methodName, operation, executionTime, monitorPerformance.threshold());
            } else {
                log.info("✅ 性能监控 - {}#{} [{}] 执行时间: {}ms", 
                    className, methodName, operation, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.error("❌ 方法异常 - {}#{} [{}] 执行时间: {}ms, 异常: {}", 
                className, methodName, operation, executionTime, e.getMessage());
            throw e;
        }
    }
}

// 使用示例（在你的Service中使用）
/*
@Service
public class UserService {
    
    @MonitorPerformance("查询用户列表")
    public List<User> getUserList() {
        // 模拟耗时操作
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return userMapper.selectList(null);
    }
    
    @MonitorPerformance(value = "复杂计算", threshold = 2000)
    public void complexCalculation() {
        try {
            Thread.sleep(3000); // 模拟超过阈值的操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
*/