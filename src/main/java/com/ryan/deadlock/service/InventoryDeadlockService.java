package com.ryan.deadlock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ryan.common.aspect.PerformanceAspect.MonitorPerformance;
import com.ryan.common.aspect.RetryAspect.Retry;
import com.ryan.deadlock.entity.Inventory;
import com.ryan.deadlock.mapper.InventoryMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 库存扣减死锁场景演示服务
 * 
 * 死锁原理：
 * 1. 订单A：需要商品1、商品2，按顺序 product_id=1001, 1002 加锁
 * 2. 订单B：需要商品2、商品1，按顺序 product_id=1002, 1001 加锁
 * 3. 并发执行时产生死锁
 */
@Slf4j
@Service
public class InventoryDeadlockService {
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    /**
     * 多商品库存扣减 - 容易产生死锁的版本
     * 
     * @param productIds 商品ID列表
     * @param quantities 对应的扣减数量列表
     */
    @MonitorPerformance(value = "库存扣减-死锁版本", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void reduceStockWithDeadlock(List<Integer> productIds, List<Integer> quantities) {
        log.info("开始扣减库存(死锁版本): products={}, quantities={}", productIds, quantities);
        
        if (productIds.size() != quantities.size()) {
            throw new RuntimeException("商品ID和数量列表长度不匹配");
        }
        
        // 按传入顺序加锁 (可能导致死锁的关键点)
        for (int i = 0; i < productIds.size(); i++) {
            Integer productId = productIds.get(i);
            Integer quantity = quantities.get(i);
            
            // 获取商品库存锁
            Inventory inventory = inventoryMapper.selectByProductIdForUpdate(productId);
            if (inventory == null) {
                throw new RuntimeException("商品不存在: " + productId);
            }
            
            // 模拟一些处理时间，增加死锁概率
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 检查库存
            if (inventory.getStockQuantity() < quantity) {
                throw new RuntimeException(String.format("商品%s库存不足，当前库存：%d，需要：%d", 
                    productId, inventory.getStockQuantity(), quantity));
            }
        }
        
        // 执行扣减
        for (int i = 0; i < productIds.size(); i++) {
            Integer productId = productIds.get(i);
            Integer quantity = quantities.get(i);
            
            int result = inventoryMapper.reduceStock(productId, quantity);
            if (result == 0) {
                throw new RuntimeException("扣减库存失败: " + productId);
            }
            
            log.info("扣减库存成功: productId={}, quantity={}", productId, quantity);
        }
        
        log.info("库存扣减完成(死锁版本): products={}", productIds);
    }
    
    /**
     * 多商品库存扣减 - 避免死锁的版本（统一加锁顺序）
     */
    @MonitorPerformance(value = "库存扣减-无死锁版本", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void reduceStockWithoutDeadlock(List<Integer> productIds, List<Integer> quantities) {
        log.info("开始扣减库存(无死锁版本): products={}, quantities={}", productIds, quantities);
        
        if (productIds.size() != quantities.size()) {
            throw new RuntimeException("商品ID和数量列表长度不匹配");
        }
        
        // 关键优化：按商品ID升序批量加锁，避免死锁
        List<Inventory> inventories = inventoryMapper.selectByProductIdsForUpdate(productIds);
        
        // 验证所有商品存在
        if (inventories.size() != productIds.size()) {
            throw new RuntimeException("部分商品不存在");
        }
        
        // 检查库存并执行扣减
        for (int i = 0; i < productIds.size(); i++) {
            Integer productId = productIds.get(i);
            Integer quantity = quantities.get(i);
            
            Inventory inventory = inventories.stream()
                .filter(inv -> inv.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));
                
            // 检查库存
            if (inventory.getStockQuantity() < quantity) {
                throw new RuntimeException(String.format("商品%s库存不足，当前库存：%d，需要：%d", 
                    productId, inventory.getStockQuantity(), quantity));
            }
            
            // 执行扣减
            int result = inventoryMapper.reduceStock(productId, quantity);
            if (result == 0) {
                throw new RuntimeException("扣减库存失败: " + productId);
            }
            
            log.info("扣减库存成功: productId={}, quantity={}", productId, quantity);
        }
        
        log.info("库存扣减完成(无死锁版本): products={}", productIds);
    }
    
    /**
     * 乐观锁库存扣减 - 使用版本号避免死锁
     */
    @Retry(maxAttempts = 3, delay = 100)
    @MonitorPerformance(value = "库存扣减-乐观锁版本", threshold = 3000)  
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void reduceStockWithOptimisticLock(List<Integer> productIds, List<Integer> quantities) {
        log.info("开始扣减库存(乐观锁版本): products={}, quantities={}", productIds, quantities);
        
        if (productIds.size() != quantities.size()) {
            throw new RuntimeException("商品ID和数量列表长度不匹配");
        }
        
        // 不加行锁，先查询库存信息
        for (int i = 0; i < productIds.size(); i++) {
            Integer productId = productIds.get(i);
            Integer quantity = quantities.get(i);
            
            QueryWrapper<Inventory> query = new QueryWrapper<>();
            query.eq("product_id", productId);
            Inventory inventory = inventoryMapper.selectOne(query);
            
            if (inventory == null) {
                throw new RuntimeException("商品不存在: " + productId);
            }
            
            // 检查库存
            if (inventory.getStockQuantity() < quantity) {
                throw new RuntimeException(String.format("商品%s库存不足，当前库存：%d，需要：%d", 
                    productId, inventory.getStockQuantity(), quantity));
            }
            
            // 使用乐观锁扣减
            int result = inventoryMapper.reduceStockWithVersion(productId, quantity, inventory.getVersion());
            if (result == 0) {
                throw new RuntimeException("库存扣减失败，请重试: " + productId);
            }
            
            log.info("扣减库存成功(乐观锁): productId={}, quantity={}", productId, quantity);
        }
        
        log.info("库存扣减完成(乐观锁版本): products={}", productIds);
    }
    
    /**
     * 预留库存模式 - 两阶段提交避免死锁
     */
    @SneakyThrows
    @MonitorPerformance(value = "预留库存模式", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void reserveAndConfirmStock(List<Integer> productIds, List<Integer> quantities) {
        log.info("开始预留库存: products={}, quantities={}", productIds, quantities);
        
        if (productIds.size() != quantities.size()) {
            throw new RuntimeException("商品ID和数量列表长度不匹配");
        }
        
        // 第一阶段：预留库存（按顺序加锁避免死锁）
        List<Inventory> inventories = inventoryMapper.selectByProductIdsForUpdate(productIds);
        
        try {
            // 预留库存
            for (int i = 0; i < productIds.size(); i++) {
                Integer productId = productIds.get(i);
                Integer quantity = quantities.get(i);
                
                int result = inventoryMapper.reserveStock(productId, quantity);
                if (result == 0) {
                    throw new RuntimeException("预留库存失败: " + productId);
                }
                
                log.info("预留库存成功: productId={}, quantity={}", productId, quantity);
            }
            
            // 模拟业务处理时间
            TimeUnit.MILLISECONDS.sleep(100);
            
            // 第二阶段：确认扣减库存
            for (int i = 0; i < productIds.size(); i++) {
                Integer productId = productIds.get(i);
                Integer quantity = quantities.get(i);
                
                // 从预留转为实际扣减
                int releaseResult = inventoryMapper.releaseReservedStock(productId, quantity);
                int reduceResult = inventoryMapper.reduceStock(productId, quantity);
                
                if (releaseResult == 0 || reduceResult == 0) {
                    throw new RuntimeException("确认扣减库存失败: " + productId);
                }
                
                log.info("确认扣减库存成功: productId={}, quantity={}", productId, quantity);
            }
            
        } catch (Exception e) {
            // 失败时释放所有预留库存
            for (int i = 0; i < productIds.size(); i++) {
                Integer productId = productIds.get(i);
                Integer quantity = quantities.get(i);
                
                try {
                    inventoryMapper.releaseReservedStock(productId, quantity);
                    log.info("释放预留库存: productId={}, quantity={}", productId, quantity);
                } catch (Exception ex) {
                    log.error("释放预留库存失败: productId={}, error={}", productId, ex.getMessage());
                }
            }
            throw e;
        }
        
        log.info("预留库存模式完成: products={}", productIds);
    }
    
    /**
     * 模拟并发库存扣减死锁场景
     */
    public void simulateDeadlock() {
        log.info("开始模拟库存扣减死锁场景...");
        
        Thread thread1 = new Thread(() -> {
            try {
                // 线程1：先扣商品1001，再扣1002
                reduceStockWithDeadlock(Arrays.asList(1001, 1002), Arrays.asList(5, 3));
            } catch (Exception e) {
                log.error("库存扣减线程1异常: {}", e.getMessage());
            }
        }, "Stock-Thread-1");
        
        Thread thread2 = new Thread(() -> {
            try {
                // 线程2：先扣商品1002，再扣1001 (顺序相反，制造死锁)
                reduceStockWithDeadlock(Arrays.asList(1002, 1001), Arrays.asList(2, 8));
            } catch (Exception e) {
                log.error("库存扣减线程2异常: {}", e.getMessage());
            }
        }, "Stock-Thread-2");
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("库存扣减死锁模拟完成");
    }
}