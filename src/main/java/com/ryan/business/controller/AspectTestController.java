package com.ryan.business.controller;

import com.ryan.business.entity.user.User;
import com.ryan.business.mapper.UserMapper;
import com.ryan.common.aspect.CacheAspect;
import com.ryan.common.aspect.OperationLogAspect;
import com.ryan.common.aspect.PerformanceAspect;
import com.ryan.common.aspect.RetryAspect;
import com.ryan.common.base.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * 切面测试Controller - 演示各种切面的效果
 */
@Slf4j
@RestController
@RequestMapping("/api/aspect-test")
public class AspectTestController {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CacheAspect cacheAspect;

    private final Random random = new Random();

    // ==================== 性能监控切面测试 ====================
    
    @PerformanceAspect.MonitorPerformance("快速查询用户")
    @GetMapping("/performance/fast")
    public ResultVO<List<User>> fastQuery() {
        // 快速查询，不会触发慢查询警告
        List<User> users = userMapper.selectList(null);
        return ResultVO.success(users);
    }

    @PerformanceAspect.MonitorPerformance(value = "慢速查询用户", threshold = 500)
    @GetMapping("/performance/slow")
    public ResultVO<String> slowQuery() {
        // 模拟慢查询，会触发慢查询警告
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ResultVO.success("慢查询完成");
    }

    @PerformanceAspect.MonitorPerformance(value = "测试实验", threshold = 200)
    @GetMapping("/test-aop")
    public ResultVO<String> testAOP() {
        log.info("我是业务代码，我正在执行...");

        try {
            Thread.sleep(500); // 模拟业务处理
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("我是业务代码，我执行完了！");
        return ResultVO.success("业务执行完成");
    }

    @PerformanceAspect.MonitorPerformance("异常方法测试")
    @GetMapping("/performance/error")
    public ResultVO<String> errorMethod() {
        throw new RuntimeException("这是一个测试异常，用于演示性能监控如何处理异常情况");
    }

    // ==================== 操作日志切面测试 ====================

    @OperationLogAspect.OperationLog(value = "查询用户详情", module = "用户管理", recordResult = true)
    @GetMapping("/operation/user/{id}")
    public ResultVO<User> getUserWithLog(@PathVariable Long id) {
        log.info("开始执行getUserWithLog");
        User user = userMapper.selectById(id);
        log.info("getUserWithLog执行结束");
        return ResultVO.success(user);
        
    }

    @OperationLogAspect.OperationLog(value = "批量查询用户", module = "用户管理", recordParams = true, recordResult = true)
    @PostMapping("/operation/users/batch")
    public ResultVO<List<User>> getUsersBatch(@RequestBody List<Long> ids) {
        List<User> users = userMapper.selectBatchIds(ids);
        return ResultVO.success(users);
    }

    @OperationLogAspect.OperationLog(value = "模拟用户操作异常", module = "用户管理")
    @PostMapping("/operation/error")
    public ResultVO<String> operationError(@RequestParam String errorType) {
        if ("null".equals(errorType)) {
            throw new NullPointerException("模拟空指针异常");
        } else if ("illegal".equals(errorType)) {
            throw new IllegalArgumentException("模拟参数异常");
        } else {
            throw new RuntimeException("模拟运行时异常");
        }
    }

    // ==================== 缓存切面测试 ====================

    @CacheAspect.Cacheable(key = "all_users", expireTime = 60)
    @GetMapping("/cache/users")
    public ResultVO<List<User>> getCachedUsers() {
        log.info("🔍 从数据库查询所有用户（这条日志只在缓存未命中时出现）");
        List<User> users = userMapper.selectList(null);
        return ResultVO.success(users);
    }

    @CacheAspect.Cacheable(prefix = "user_detail", expireTime = 30)
    @GetMapping("/cache/user/{id}")
    public ResultVO<User> getCachedUser(@PathVariable Long id) {
        log.info("🔍 从数据库查询用户: {} （这条日志只在缓存未命中时出现）", id);
        User user = userMapper.selectById(id);
        return ResultVO.success(user);
    }

    @CacheAspect.CacheEvict(key = "all_users")
    @PostMapping("/cache/evict/users")
    public ResultVO<String> evictUsersCache() {
        return ResultVO.success("用户列表缓存已清除");
    }

    @CacheAspect.CacheEvict(allEntries = true)
    @PostMapping("/cache/evict/all")
    public ResultVO<String> evictAllCache() {
        return ResultVO.success("所有缓存已清除");
    }

    @GetMapping("/cache/status")
    public ResultVO<String> getCacheStatus() {
        cacheAspect.printCacheStatus();
        return ResultVO.success("缓存状态已打印到日志");
    }

    // ==================== 重试切面测试 ====================

    @RetryAspect.Retry(maxAttempts = 3, delay = 1000)
    @GetMapping("/retry/unstable")
    public ResultVO<String> unstableMethod() {
        log.info("🎲 执行不稳定方法，成功率约50%");
        
        // 模拟50%的失败率
        if (random.nextDouble() < 0.5) {
            throw new RuntimeException("模拟服务不可用");
        }
        
        return ResultVO.success("方法执行成功！时间：" + System.currentTimeMillis());
    }

    @RetryAspect.Retry(maxAttempts = 5, delay = 500, useExponentialBackoff = true, backoffMultiplier = 2.0)
    @GetMapping("/retry/exponential")
    public ResultVO<String> exponentialBackoffMethod() {
        log.info("🎲 执行指数退避重试方法，成功率约30%");
        
        // 模拟30%的成功率，需要更多重试
        if (random.nextDouble() < 0.7) {
            throw new RuntimeException("模拟网络超时");
        }
        
        return ResultVO.success("指数退避重试成功！");
    }

    @RetryAspect.Retry(maxAttempts = 3, retryOn = {IllegalArgumentException.class})
    @PostMapping("/retry/specific-exception")
    public ResultVO<String> specificExceptionRetry(@RequestParam String data) {
        log.info("🎯 只对特定异常进行重试");
        
        if ("empty".equals(data)) {
            throw new IllegalArgumentException("数据不能为空（会重试）");
        } else if ("invalid".equals(data)) {
            throw new RuntimeException("无效数据格式（不会重试）");
        }
        
        return ResultVO.success("数据处理成功：" + data);
    }

    // ==================== 综合测试 ====================

    @PerformanceAspect.MonitorPerformance("综合切面测试")
    @OperationLogAspect.OperationLog(value = "执行综合切面测试", module = "系统测试", recordParams = true, recordResult = true)
    @CacheAspect.Cacheable(key = "comprehensive_test", expireTime = 120)
    @RetryAspect.Retry(maxAttempts = 2, delay = 500)
    @PostMapping("/comprehensive")
    public ResultVO<String> comprehensiveTest(@RequestParam String testType) {
        log.info("🧪 执行综合切面测试，类型：{}", testType);
        
        // 模拟不同的处理逻辑
        switch (testType) {
            case "success":
                try {
                    Thread.sleep(200); // 模拟处理时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return ResultVO.success("综合测试成功完成");
                
            case "slow":
                try {
                    Thread.sleep(1500); // 触发慢查询警告
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return ResultVO.success("慢速综合测试完成");
                
            case "retry":
                if (random.nextDouble() < 0.6) {
                    throw new RuntimeException("模拟处理失败，触发重试");
                }
                return ResultVO.success("重试后成功完成");
                
            default:
                throw new IllegalArgumentException("不支持的测试类型：" + testType);
        }
    }
}