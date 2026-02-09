package com.ryan.business.controller;

import com.ryan.common.aspect.RetryAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

// 模拟一个不稳定的外部服务
@Component
@Slf4j
class UnstableExternalService {
    private final Random random = new Random();
    private int callCount = 0;

    @RetryAspect.Retry(maxAttempts = 5, delay = 500, useExponentialBackoff = true)
    public String callExternalApi(String param) {
        callCount++;
        log.info("🌐 模拟调用外部API，参数: {}, 调用次数: {}", param, callCount);
        
        // 模拟70%的失败率
        if (random.nextDouble() < 0.7) {
            throw new RuntimeException("网络超时或服务不可用");
        }
        
        return "API调用成功，返回数据: " + param + "_response_" + System.currentTimeMillis();
    }

    @RetryAspect.Retry(maxAttempts = 3, delay = 1000, retryOn = {IllegalArgumentException.class, RuntimeException.class})
    public void processData(String data) {
        log.info("📊 处理数据: {}", data);
        
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        // 模拟处理失败
        if (random.nextDouble() < 0.5) {
            throw new RuntimeException("数据处理失败");
        }
        
        log.info("✅ 数据处理成功: {}", data);
    }

    // 重置计数器（用于测试）
    public void resetCallCount() {
        this.callCount = 0;
    }
}