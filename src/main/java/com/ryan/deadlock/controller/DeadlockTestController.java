package com.ryan.deadlock.controller;

import com.ryan.common.base.ResultVO;
import com.ryan.deadlock.aspect.DeadlockMonitorAspect;
import com.ryan.deadlock.monitor.DeadlockMonitor;
import com.ryan.deadlock.service.IndexDeadlockService;
import com.ryan.deadlock.service.InventoryDeadlockService;
import com.ryan.deadlock.service.TransferDeadlockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库死锁测试控制器
 * 
 * 提供各种死锁场景的测试接口：
 * 1. 转账死锁场景
 * 2. 库存扣减死锁场景  
 * 3. 索引锁死锁场景
 * 4. 死锁监控和统计
 */
@Slf4j
@RestController
@RequestMapping("/deadlock")
public class DeadlockTestController {
    
    @Autowired
    private TransferDeadlockService transferDeadlockService;
    
    @Autowired
    private InventoryDeadlockService inventoryDeadlockService;
    
    @Autowired
    private IndexDeadlockService indexDeadlockService;
    
    @Autowired
    private DeadlockMonitor deadlockMonitor;
    
    @Autowired
    private DeadlockMonitorAspect deadlockMonitorAspect;
    
    /**
     * 获取死锁学习主页信息
     */
    @GetMapping("/")
    public ResultVO<Map<String, Object>> getDeadlockInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("title", "数据库死锁实战学习系统");
        info.put("version", "1.0.0");
        info.put("description", "提供多种真实场景的数据库死锁演示，帮助理解死锁产生原理和解决方案");
        
        Map<String, String> scenarios = new HashMap<>();
        scenarios.put("transfer", "转账死锁 - 模拟银行转账场景的死锁");
        scenarios.put("inventory", "库存死锁 - 模拟电商库存扣减的死锁");
        scenarios.put("index", "索引死锁 - 模拟Gap锁和Next-Key锁死锁");
        info.put("scenarios", scenarios);
        
        Map<String, String> apis = new HashMap<>();
        apis.put("GET /deadlock/stats", "获取死锁统计信息");
        apis.put("POST /deadlock/transfer/simulate", "模拟转账死锁");
        apis.put("POST /deadlock/inventory/simulate", "模拟库存死锁");
        apis.put("POST /deadlock/index/simulate", "模拟索引死锁");
        apis.put("GET /deadlock/monitor/status", "获取实时死锁监控状态");
        info.put("apis", apis);
        
        return ResultVO.success(info);
    }
    
    // ==================== 转账死锁测试 ====================
    
    /**
     * 模拟转账死锁场景
     */
    @PostMapping("/transfer/simulate")
    public ResultVO<String> simulateTransferDeadlock() {
        log.info("开始模拟转账死锁场景...");
        
        try {
            transferDeadlockService.simulateDeadlock();
            return ResultVO.success("转账死锁模拟完成，请查看日志了解详细过程");
        } catch (Exception e) {
            log.error("转账死锁模拟异常: {}", e.getMessage());
            return ResultVO.error("转账死锁模拟失败: " + e.getMessage());
        }
    }
    
    /**
     * 单次转账测试 - 死锁版本
     */
    @PostMapping("/transfer/deadlock-version")
    public ResultVO<String> transferWithDeadlock(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount) {
        
        try {
            transferDeadlockService.transferWithDeadlock(fromAccount, toAccount, amount);
            return ResultVO.success("转账成功");
        } catch (Exception e) {
            log.error("转账异常: {}", e.getMessage());
            return ResultVO.error("转账失败: " + e.getMessage());
        }
    }
    
    /**
     * 单次转账测试 - 无死锁版本
     */
    @PostMapping("/transfer/no-deadlock-version")
    public ResultVO<String> transferWithoutDeadlock(
            @RequestParam String fromAccount,
            @RequestParam String toAccount,
            @RequestParam BigDecimal amount) {
        
        try {
            transferDeadlockService.transferWithoutDeadlock(fromAccount, toAccount, amount);
            return ResultVO.success("转账成功");
        } catch (Exception e) {
            log.error("转账异常: {}", e.getMessage());
            return ResultVO.error("转账失败: " + e.getMessage());
        }
    }
    
    /**
     * 并发转账压力测试
     */
    @PostMapping("/transfer/concurrent-test")
    public ResultVO<Map<String, Object>> concurrentTransferTest(@RequestParam(defaultValue = "10") int threadCount) {
        log.info("开始并发转账压力测试，线程数: {}", threadCount);
        
        long startTime = System.currentTimeMillis();
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    if (threadIndex % 2 == 0) {
                        transferDeadlockService.transferWithDeadlock("ACC001", "ACC002", new BigDecimal("10.00"));
                    } else {
                        transferDeadlockService.transferWithDeadlock("ACC002", "ACC001", new BigDecimal("20.00"));
                    }
                } catch (Exception e) {
                    log.error("并发转账异常 (线程{}): {}", threadIndex, e.getMessage());
                }
            });
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("threadCount", threadCount);
        result.put("executionTime", endTime - startTime);
        result.put("deadlockCount", deadlockMonitorAspect.getDeadlockExceptionCount());
        result.put("message", "并发转账压力测试完成");
        
        return ResultVO.success(result);
    }
    
    // ==================== 库存死锁测试 ====================
    
    /**
     * 模拟库存扣减死锁场景
     */
    @PostMapping("/inventory/simulate")
    public ResultVO<String> simulateInventoryDeadlock() {
        log.info("开始模拟库存扣减死锁场景...");
        
        try {
            inventoryDeadlockService.simulateDeadlock();
            return ResultVO.success("库存死锁模拟完成，请查看日志了解详细过程");
        } catch (Exception e) {
            log.error("库存死锁模拟异常: {}", e.getMessage());
            return ResultVO.error("库存死锁模拟失败: " + e.getMessage());
        }
    }
    
    /**
     * 库存扣减测试 - 死锁版本
     */
    @PostMapping("/inventory/reduce-deadlock-version")
    public ResultVO<String> reduceStockWithDeadlock(
            @RequestParam List<Integer> productIds,
            @RequestParam List<Integer> quantities) {
        
        try {
            inventoryDeadlockService.reduceStockWithDeadlock(productIds, quantities);
            return ResultVO.success("库存扣减成功");
        } catch (Exception e) {
            log.error("库存扣减异常: {}", e.getMessage());
            return ResultVO.error("库存扣减失败: " + e.getMessage());
        }
    }
    
    /**
     * 库存扣减测试 - 无死锁版本
     */
    @PostMapping("/inventory/reduce-no-deadlock-version")
    public ResultVO<String> reduceStockWithoutDeadlock(
            @RequestParam List<Integer> productIds,
            @RequestParam List<Integer> quantities) {
        
        try {
            inventoryDeadlockService.reduceStockWithoutDeadlock(productIds, quantities);
            return ResultVO.success("库存扣减成功");
        } catch (Exception e) {
            log.error("库存扣减异常: {}", e.getMessage());
            return ResultVO.error("库存扣减失败: " + e.getMessage());
        }
    }
    
    /**
     * 预留库存模式测试
     */
    @PostMapping("/inventory/reserve-confirm")
    public ResultVO<String> reserveAndConfirmStock(
            @RequestParam List<Integer> productIds,
            @RequestParam List<Integer> quantities) {
        
        try {
            inventoryDeadlockService.reserveAndConfirmStock(productIds, quantities);
            return ResultVO.success("预留库存操作成功");
        } catch (Exception e) {
            log.error("预留库存操作异常: {}", e.getMessage());
            return ResultVO.error("预留库存操作失败: " + e.getMessage());
        }
    }
    
    // ==================== 索引死锁测试 ====================
    
    /**
     * 模拟Gap锁死锁场景
     */
    @PostMapping("/index/simulate-gap-lock")
    public ResultVO<String> simulateGapLockDeadlock() {
        log.info("开始模拟Gap锁死锁场景...");
        
        try {
            indexDeadlockService.simulateGapLockDeadlock();
            return ResultVO.success("Gap锁死锁模拟完成，请查看日志了解详细过程");
        } catch (Exception e) {
            log.error("Gap锁死锁模拟异常: {}", e.getMessage());
            return ResultVO.error("Gap锁死锁模拟失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟Next-Key锁死锁场景
     */
    @PostMapping("/index/simulate-next-key-lock")
    public ResultVO<String> simulateNextKeyLockDeadlock() {
        log.info("开始模拟Next-Key锁死锁场景...");
        
        try {
            indexDeadlockService.simulateNextKeyLockDeadlock();
            return ResultVO.success("Next-Key锁死锁模拟完成，请查看日志了解详细过程");
        } catch (Exception e) {
            log.error("Next-Key锁死锁模拟异常: {}", e.getMessage());
            return ResultVO.error("Next-Key锁死锁模拟失败: " + e.getMessage());
        }
    }
    
    /**
     * Gap锁死锁场景1测试
     */
    @PostMapping("/index/gap-lock-scenario1")
    public ResultVO<String> gapLockDeadlockScenario1() {
        try {
            indexDeadlockService.gapLockDeadlockScenario1();
            return ResultVO.success("Gap锁场景1执行成功");
        } catch (Exception e) {
            log.error("Gap锁场景1异常: {}", e.getMessage());
            return ResultVO.error("Gap锁场景1执行失败: " + e.getMessage());
        }
    }
    
    // ==================== 死锁监控和统计 ====================
    
    /**
     * 获取死锁统计信息
     */
    @GetMapping("/stats")
    public ResultVO<Map<String, Object>> getDeadlockStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("monitorStats", deadlockMonitor.getDeadlockStatistics());
        stats.put("exceptionCount", deadlockMonitorAspect.getDeadlockExceptionCount());
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResultVO.success(stats);
    }
    
    /**
     * 获取实时死锁监控状态
     */
    @GetMapping("/monitor/status")
    public ResultVO<Map<String, Object>> getMonitorStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 检查当前死锁
        DeadlockMonitor.DeadlockInfo currentDeadlock = deadlockMonitor.checkAndReportDeadlock();
        status.put("currentDeadlock", currentDeadlock);
        
        // 获取锁等待信息
        List<Map<String, Object>> lockWaits = deadlockMonitor.getCurrentLockWaits();
        status.put("lockWaits", lockWaits);
        
        // 获取InnoDB状态
        String innodbStatus = deadlockMonitor.getInnodbStatus();
        status.put("hasInnodbStatus", innodbStatus != null);
        
        status.put("timestamp", System.currentTimeMillis());
        
        return ResultVO.success(status);
    }
    
    /**
     * 重置死锁统计计数器
     */
    @PostMapping("/stats/reset")
    public ResultVO<String> resetDeadlockStats() {
        deadlockMonitor.resetStatistics();
        deadlockMonitorAspect.resetDeadlockExceptionCount();
        return ResultVO.success("死锁统计计数器已重置");
    }
    
    /**
     * 获取InnoDB详细状态信息
     */
    @GetMapping("/monitor/innodb-status")
    public ResultVO<String> getInnodbStatus() {
        String status = deadlockMonitor.getInnodbStatus();
        if (status != null) {
            return ResultVO.success(status);
        } else {
            return ResultVO.error("无法获取InnoDB状态信息");
        }
    }
    
    // ==================== 综合测试场景 ====================
    
    /**
     * 运行所有死锁场景的综合测试
     */
    @PostMapping("/comprehensive-test")
    public ResultVO<Map<String, Object>> comprehensiveDeadlockTest() {
        log.info("开始运行综合死锁测试...");
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 转账死锁测试
            log.info("执行转账死锁测试...");
            transferDeadlockService.simulateDeadlock();
            Thread.sleep(1000);
            
            // 2. 库存死锁测试
            log.info("执行库存死锁测试...");
            inventoryDeadlockService.simulateDeadlock();
            Thread.sleep(1000);
            
            // 3. Gap锁死锁测试
            log.info("执行Gap锁死锁测试...");
            indexDeadlockService.simulateGapLockDeadlock();
            Thread.sleep(1000);
            
            // 4. Next-Key锁死锁测试
            log.info("执行Next-Key锁死锁测试...");
            indexDeadlockService.simulateNextKeyLockDeadlock();
            
            long endTime = System.currentTimeMillis();
            
            result.put("status", "completed");
            result.put("executionTime", endTime - startTime);
            result.put("totalDeadlocks", deadlockMonitorAspect.getDeadlockExceptionCount());
            result.put("statistics", deadlockMonitor.getDeadlockStatistics());
            result.put("message", "综合死锁测试完成，共检测到 " + 
                deadlockMonitorAspect.getDeadlockExceptionCount() + " 次死锁异常");
            
            return ResultVO.success(result);
            
        } catch (Exception e) {
            log.error("综合死锁测试异常: {}", e.getMessage());
            result.put("status", "failed");
            result.put("error", e.getMessage());
            return ResultVO.error("综合死锁测试失败: " + e.getMessage(), result);
        }
    }
}