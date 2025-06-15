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
import java.util.Random;

/**
 * 重试切面 - 自动重试失败的方法调用
 */
@Slf4j
@Aspect
@Component
public class RetryAspect {

    /**
     * 重试注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Retry {
        int maxAttempts() default 3; // 最大重试次数
        long delay() default 1000; // 重试间隔(毫秒)
        Class<? extends Throwable>[] retryOn() default {Exception.class}; // 哪些异常需要重试
        boolean useExponentialBackoff() default false; // 是否使用指数退避
        double backoffMultiplier() default 2.0; // 退避倍数
    }

    @Around("@annotation(retry)")
    public Object handleRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        String methodName = joinPoint.getTarget().getClass().getSimpleName() + "#" + joinPoint.getSignature().getName();
        int maxAttempts = retry.maxAttempts();
        long delay = retry.delay();
        Class<? extends Throwable>[] retryOn = retry.retryOn();
        
        log.info("🔄 开始执行方法: {} (最大重试{}次)", methodName, maxAttempts);
        
        Throwable lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("⏳ 第{}次尝试执行: {}", attempt, methodName);
                Object result = joinPoint.proceed();
                
                if (attempt > 1) {
                    log.info("✅ 重试成功! {} 在第{}次尝试后成功执行", methodName, attempt);
                } else {
                    log.info("✅ 首次执行成功: {}", methodName);
                }
                
                return result;
                
            } catch (Throwable e) {
                lastException = e;
                
                // 检查是否是需要重试的异常类型
                boolean shouldRetry = false;
                for (Class<? extends Throwable> retryException : retryOn) {
                    if (retryException.isAssignableFrom(e.getClass())) {
                        shouldRetry = true;
                        break;
                    }
                }
                
                if (!shouldRetry) {
                    log.error("❌ 异常类型不在重试范围内: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                    throw e;
                }
                
                if (attempt == maxAttempts) {
                    log.error("❌ 所有重试都失败了! {} 在{}次尝试后仍然失败: {}", 
                        methodName, maxAttempts, e.getMessage());
                    break;
                } else {
                    log.warn("⚠️ 第{}次尝试失败: {} - {}, 准备重试...", attempt, methodName, e.getMessage());
                    
                    // 计算延迟时间
                    long currentDelay = delay;
                    if (retry.useExponentialBackoff()) {
                        currentDelay = (long) (delay * Math.pow(retry.backoffMultiplier(), attempt - 1));
                    }
                    
                    if (currentDelay > 0) {
                        log.info("⏰ 等待{}ms后进行第{}次重试...", currentDelay, attempt + 1);
                        try {
                            Thread.sleep(currentDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("重试被中断", ie);
                        }
                    }
                }
            }
        }
        
        throw lastException;
    }
}



// 测试Controller
/*
@RestController
@RequestMapping("/api/test")
public class RetryTestController {
    
    @Autowired
    private UnstableExternalService externalService;
    
    @GetMapping("/retry-api")
    public ResultVO<String> testRetryApi(@RequestParam String param) {
        try {
            String result = externalService.callExternalApi(param);
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error("API调用失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/retry-process")
    public ResultVO<String> testRetryProcess(@RequestParam String data) {
        try {
            externalService.processData(data);
            return ResultVO.success("数据处理成功");
        } catch (Exception e) {
            return ResultVO.error("数据处理失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset")
    public ResultVO<String> resetCounter() {
        externalService.resetCallCount();
        return ResultVO.success("计数器已重置");
    }
}
*/