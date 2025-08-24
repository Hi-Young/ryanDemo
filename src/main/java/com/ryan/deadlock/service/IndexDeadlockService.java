package com.ryan.deadlock.service;

import com.ryan.common.aspect.PerformanceAspect.MonitorPerformance;
import com.ryan.deadlock.entity.OrderLock;
import com.ryan.deadlock.mapper.OrderLockMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 索引死锁场景演示服务
 * 
 * Gap锁死锁原理：
 * 1. 事务A：查询某个范围的数据，产生Gap锁
 * 2. 事务B：在Gap范围内插入数据，等待Gap锁释放  
 * 3. 事务A：尝试在同一Gap范围内插入数据，等待插入意向锁
 * 4. 形成循环等待，产生死锁
 * 
 * Next-Key锁死锁原理：
 * 1. Next-Key锁 = Record锁 + Gap锁
 * 2. 在可重复读隔离级别下，范围查询会产生Next-Key锁
 * 3. 并发的范围查询和插入操作容易产生死锁
 */
@Slf4j
@Service
public class IndexDeadlockService {
    
    @Autowired
    private OrderLockMapper orderLockMapper;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Gap锁死锁场景1：并发范围查询 + 插入
     */
    @MonitorPerformance(value = "Gap锁死锁-范围查询插入", threshold = 5000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public void gapLockDeadlockScenario1() {
        log.info("开始Gap锁死锁场景1 - 线程: {}", Thread.currentThread().getName());
        
        String startTime = "2024-10-01 10:00:00";
        String endTime = "2024-10-01 12:00:00";
        
        // 范围查询，产生Gap锁
        List<OrderLock> orders = orderLockMapper.selectOrdersByTimeRange(startTime, endTime);
        log.info("查询到订单数量: {}", orders.size());
        
        // 模拟处理时间
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 尝试在Gap范围内插入数据 (可能产生死锁)
        OrderLock newOrder = new OrderLock(
            "ORD20241001" + System.currentTimeMillis() % 1000,
            1,
            Arrays.asList(1001, 1003),
            new BigDecimal("1999.00")
        );
        
        int result = orderLockMapper.insert(newOrder);
        log.info("插入订单结果: {}, 订单号: {}", result, newOrder.getOrderNo());
    }
    
    /**
     * Gap锁死锁场景2：并发用户订单查询 + 状态更新
     */
    @MonitorPerformance(value = "Gap锁死锁-用户查询更新", threshold = 5000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public void gapLockDeadlockScenario2(Integer userId) {
        log.info("开始Gap锁死锁场景2 - 用户: {}, 线程: {}", userId, Thread.currentThread().getName());
        
        // 查询用户的待处理订单 (产生Gap锁)
        List<OrderLock> pendingOrders = orderLockMapper.selectPendingOrdersByUserIdForUpdate(userId);
        log.info("用户{}待处理订单数量: {}", userId, pendingOrders.size());
        
        // 模拟处理时间，增加死锁概率
        try {
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 更新订单状态 (可能与其他事务的插入产生死锁)
        for (OrderLock order : pendingOrders) {
            int result = orderLockMapper.updateOrderStatus(order.getOrderNo(), OrderLock.OrderStatus.PROCESSING.getCode());
            log.info("更新订单状态: orderNo={}, result={}", order.getOrderNo(), result);
        }
        
        // 同时插入一个新订单 (可能产生死锁)
        OrderLock newOrder = new OrderLock(
            "ORD20241001" + userId + System.currentTimeMillis() % 1000,
            userId,
            Arrays.asList(1002, 1004),
            new BigDecimal("2999.00")
        );
        
        int insertResult = orderLockMapper.insert(newOrder);
        log.info("插入新订单结果: {}, 订单号: {}", insertResult, newOrder.getOrderNo());
    }
    
    /**
     * Next-Key锁死锁场景：并发范围查询
     */
    @MonitorPerformance(value = "Next-Key锁死锁", threshold = 5000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public void nextKeyLockDeadlock(String threadName) {
        log.info("开始Next-Key锁死锁场景 - 线程: {}", threadName);
        
        // 根据线程名决定查询顺序，制造死锁
        if ("Thread-A".equals(threadName)) {
            // 线程A：先查询用户1，再查询用户2
            queryAndUpdateUserOrders(1);
            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) {}
            queryAndUpdateUserOrders(2);
        } else {
            // 线程B：先查询用户2，再查询用户1 (顺序相反)
            queryAndUpdateUserOrders(2);
            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) {}
            queryAndUpdateUserOrders(1);
        }
        
        log.info("Next-Key锁死锁场景完成 - 线程: {}", threadName);
    }
    
    /**
     * 查询并更新用户订单 (产生Next-Key锁)
     */
    private void queryAndUpdateUserOrders(Integer userId) {
        log.info("查询用户{}的订单 - 线程: {}", userId, Thread.currentThread().getName());
        
        // SELECT ... FOR UPDATE 在可重复读下产生Next-Key锁
        List<OrderLock> orders = orderLockMapper.selectPendingOrdersByUserIdForUpdate(userId);
        log.info("用户{}的待处理订单数量: {}", userId, orders.size());
        
        // 模拟一些处理时间
        try {
            TimeUnit.MILLISECONDS.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 尝试更新订单状态
        for (OrderLock order : orders) {
            orderLockMapper.updateOrderStatus(order.getOrderNo(), OrderLock.OrderStatus.PROCESSING.getCode());
            log.info("更新订单状态完成: {}", order.getOrderNo());
        }
    }
    
    /**
     * 避免Gap锁死锁的优化方案
     */
    @MonitorPerformance(value = "避免Gap锁死锁", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED) // 降低隔离级别
    public void avoidGapLockDeadlock() {
        log.info("开始避免Gap锁死锁的优化方案");
        
        String startTime = "2024-10-01 10:00:00";
        String endTime = "2024-10-01 12:00:00";
        
        // 使用READ_COMMITTED隔离级别，避免Gap锁
        List<OrderLock> orders = orderLockMapper.selectOrdersByTimeRange(startTime, endTime);
        log.info("查询到订单数量: {}", orders.size());
        
        // 分批处理，减少锁持有时间
        for (OrderLock order : orders) {
            orderLockMapper.updateOrderStatus(order.getOrderNo(), OrderLock.OrderStatus.PROCESSING.getCode());
            log.info("处理订单: {}", order.getOrderNo());
        }
        
        log.info("避免Gap锁死锁方案完成");
    }
    
    /**
     * 使用唯一索引避免插入死锁
     */
    @MonitorPerformance(value = "唯一索引避免死锁", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void avoidInsertDeadlockWithUniqueIndex(String orderNo, Integer userId) {
        log.info("使用唯一索引避免插入死锁: orderNo={}", orderNo);
        
        try {
            // 利用唯一索引，重复插入会直接报错而不是等待
            OrderLock newOrder = new OrderLock(orderNo, userId, Arrays.asList(1001), new BigDecimal("999.00"));
            int result = orderLockMapper.insert(newOrder);
            log.info("插入订单成功: orderNo={}, result={}", orderNo, result);
            
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                log.warn("订单号重复，跳过插入: {}", orderNo);
                // 业务处理：订单已存在，直接返回或更新状态
            } else {
                throw e;
            }
        }
    }
    
    /**
     * 模拟Gap锁死锁场景
     */
    public void simulateGapLockDeadlock() {
        log.info("开始模拟Gap锁死锁场景...");
        
        Thread thread1 = new Thread(() -> {
            try {
                gapLockDeadlockScenario1();
            } catch (Exception e) {
                log.error("Gap锁死锁线程1异常: {}", e.getMessage());
            }
        }, "Gap-Lock-Thread-1");
        
        Thread thread2 = new Thread(() -> {
            try {
                gapLockDeadlockScenario2(1);
            } catch (Exception e) {
                log.error("Gap锁死锁线程2异常: {}", e.getMessage());
            }
        }, "Gap-Lock-Thread-2");
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Gap锁死锁模拟完成");
    }
    
    /**
     * 模拟Next-Key锁死锁场景
     */
    public void simulateNextKeyLockDeadlock() {
        log.info("开始模拟Next-Key锁死锁场景...");
        
        Thread threadA = new Thread(() -> {
            try {
                nextKeyLockDeadlock("Thread-A");
            } catch (Exception e) {
                log.error("Next-Key锁死锁线程A异常: {}", e.getMessage());
            }
        }, "Next-Key-Thread-A");
        
        Thread threadB = new Thread(() -> {
            try {
                nextKeyLockDeadlock("Thread-B");
            } catch (Exception e) {
                log.error("Next-Key锁死锁线程B异常: {}", e.getMessage());
            }
        }, "Next-Key-Thread-B");
        
        threadA.start();
        threadB.start();
        
        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Next-Key锁死锁模拟完成");
    }
}