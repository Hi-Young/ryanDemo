package com.ryan.deadlock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ryan.common.aspect.PerformanceAspect.MonitorPerformance;
import com.ryan.common.aspect.RetryAspect.Retry;
import com.ryan.deadlock.entity.Account;
import com.ryan.deadlock.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 转账死锁场景演示服务
 * 
 * 死锁原理：
 * 1. 事务A：先锁账户1，再锁账户2
 * 2. 事务B：先锁账户2，再锁账户1  
 * 3. 当两个事务并发执行时，就会产生死锁
 */
@Slf4j
@Service
public class TransferDeadlockService {
    
    @Autowired
    private AccountMapper accountMapper;
    
    // 注入自己的代理，确保事务生效
    @Autowired
    private TransferDeadlockService self;
    
    /**
     * 转账操作 - 容易产生死锁的版本
     * 
     * @param fromAccountNo 转出账户
     * @param toAccountNo 转入账户  
     * @param amount 转账金额
     */
    @MonitorPerformance(value = "转账操作-死锁版本", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void transferWithDeadlock(String fromAccountNo, String toAccountNo, BigDecimal amount) {
        String threadName = Thread.currentThread().getName();
        
        // 检查事务状态
        boolean isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] 事务状态检查: 事务活跃={}, 事务名称={}", threadName, isTransactionActive, transactionName);
        
        log.info("[{}] 开始转账: {} -> {}, 金额: {}", threadName, fromAccountNo, toAccountNo, amount);
        
        // 先获取转出账户锁 (可能导致死锁的关键点1)
        log.info("[{}] 准备获取转出账户{}的锁", threadName, fromAccountNo);
        Account fromAccount = accountMapper.selectByAccountNoForUpdate(fromAccountNo);
        log.info("[{}] 已获取转出账户{}的锁", threadName, fromAccountNo);
        
        if (fromAccount == null) {
            throw new RuntimeException("转出账户不存在: " + fromAccountNo);
        }
        
        // 模拟一些处理时间，增加死锁概率
        try {
            log.info("[{}] 开始睡眠10ms", threadName);
            TimeUnit.MILLISECONDS.sleep(100);
            log.info("[{}] 睡眠结束", threadName);
            
            // 增加一些查询操作，增加死锁概率
            accountMapper.selectByAccountNo(fromAccountNo);
            accountMapper.selectByAccountNo(toAccountNo);
        } catch (InterruptedException e) {
            log.info("[{}] 线程被中断", threadName);
            Thread.currentThread().interrupt();
        }
        
        // 再获取转入账户锁 (可能导致死锁的关键点2)
        log.info("[{}] 准备获取转入账户{}的锁", threadName, toAccountNo);
        Account toAccount = accountMapper.selectByAccountNoForUpdate(toAccountNo);
        log.info("[{}] 已获取转入账户{}的锁", threadName, toAccountNo);
        
        if (toAccount == null) {
            throw new RuntimeException("转入账户不存在: " + toAccountNo);
        }
        
        // 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("账户余额不足: " + fromAccount.getBalance());
        }
        
        // 执行转账
        log.info("[{}] 开始执行转账", threadName);
        accountMapper.updateBalanceByAccountNo(fromAccountNo, amount.negate());
        accountMapper.updateBalanceByAccountNo(toAccountNo, amount);
        
        log.info("[{}] 转账完成: {} -> {}, 金额: {}", threadName, fromAccountNo, toAccountNo, amount);
    }
    
    /**
     * 转账操作 - 避免死锁的版本（统一加锁顺序）
     */
    @MonitorPerformance(value = "转账操作-无死锁版本", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void transferWithoutDeadlock(String fromAccountNo, String toAccountNo, BigDecimal amount) {
        log.info("开始转账(无死锁): {} -> {}, 金额: {}", fromAccountNo, toAccountNo, amount);
        
        // 关键优化：按账户号字母顺序加锁，避免死锁
        String firstLockAccount = fromAccountNo.compareTo(toAccountNo) < 0 ? fromAccountNo : toAccountNo;
        String secondLockAccount = fromAccountNo.compareTo(toAccountNo) < 0 ? toAccountNo : fromAccountNo;
        
        Account firstAccount = accountMapper.selectByAccountNoForUpdate(firstLockAccount);
        Account secondAccount = accountMapper.selectByAccountNoForUpdate(secondLockAccount);
        
        Account fromAccount = fromAccountNo.equals(firstLockAccount) ? firstAccount : secondAccount;
        Account toAccount = toAccountNo.equals(firstLockAccount) ? firstAccount : secondAccount;
        
        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("账户不存在");
        }
        
        // 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("账户余额不足: " + fromAccount.getBalance());
        }
        
        // 执行转账
        accountMapper.updateBalanceByAccountNo(fromAccountNo, amount.negate());
        accountMapper.updateBalanceByAccountNo(toAccountNo, amount);
        
        log.info("转账完成(无死锁): {} -> {}, 金额: {}", fromAccountNo, toAccountNo, amount);
    }
    
    /**
     * 乐观锁转账 - 使用版本号避免死锁
     */
    @Retry(maxAttempts = 3, delay = 100)
    @MonitorPerformance(value = "转账操作-乐观锁版本", threshold = 3000)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void transferWithOptimisticLock(String fromAccountNo, String toAccountNo, BigDecimal amount) {
        log.info("开始转账(乐观锁): {} -> {}, 金额: {}", fromAccountNo, toAccountNo, amount);
        
        // 不加行锁，先查询账户信息
        QueryWrapper<Account> fromQuery = new QueryWrapper<>();
        fromQuery.eq("account_no", fromAccountNo);
        Account fromAccount = accountMapper.selectOne(fromQuery);
        
        QueryWrapper<Account> toQuery = new QueryWrapper<>();
        toQuery.eq("account_no", toAccountNo);
        Account toAccount = accountMapper.selectOne(toQuery);
        
        if (fromAccount == null || toAccount == null) {
            throw new RuntimeException("账户不存在");
        }
        
        // 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("账户余额不足: " + fromAccount.getBalance());
        }
        
        // 使用乐观锁更新
        int fromResult = accountMapper.updateBalanceWithVersion(
            fromAccountNo, amount.negate(), fromAccount.getVersion()
        );
        
        if (fromResult == 0) {
            throw new RuntimeException("转出账户更新失败，请重试");
        }
        
        int toResult = accountMapper.updateBalanceWithVersion(
            toAccountNo, amount, toAccount.getVersion()
        );
        
        if (toResult == 0) {
            throw new RuntimeException("转入账户更新失败，请重试");
        }
        
        log.info("转账完成(乐观锁): {} -> {}, 金额: {}", fromAccountNo, toAccountNo, amount);
    }
    
    /**
     * 模拟并发转账场景
     */
    public void simulateDeadlock() {
        log.info("开始模拟转账死锁场景...");
        
        // 使用CountDownLatch确保两个线程真正同时开始
        final CountDownLatch readyLatch = new CountDownLatch(2);  // 等待两个线程准备就绪
        final CountDownLatch startLatch = new CountDownLatch(1);  // 统一开始信号

        Thread thread1 = new Thread(() -> {
            try {
                log.info("线程1准备就绪，等待同步开始信号");
                readyLatch.countDown();  // 表示线程1已准备就绪
                startLatch.await();      // 等待统一开始信号
                log.info("线程1收到开始信号，开始执行");
                
                self.transferWithDeadlock("ACC001", "ACC002", new BigDecimal("100.00"));
                log.info("线程1执行完成");
            } catch (Exception e) {
                log.error("转账线程1异常: {}", e.getMessage(), e);
            }
        }, "Transfer-Thread-1");

        Thread thread2 = new Thread(() -> {
            try {
                log.info("线程2准备就绪，等待同步开始信号");
                readyLatch.countDown();  // 表示线程2已准备就绪
                startLatch.await();      // 等待统一开始信号
                log.info("线程2收到开始信号，开始执行");
                
                // 故意颠倒顺序，制造死锁
                self.transferWithDeadlock("ACC002", "ACC001", new BigDecimal("200.00"));
                log.info("线程2执行完成");
            } catch (Exception e) {
                log.error("转账线程2异常: {}", e.getMessage(), e);
            }
        }, "Transfer-Thread-2");

        log.info("启动线程1和线程2");
        thread1.start();
        thread2.start();

        try {
            log.info("等待两个线程都准备就绪...");
            readyLatch.await();  // 等待两个线程都调用countDown()
            log.info("两个线程都已准备就绪，发送统一开始信号");
            startLatch.countDown();  // 发送开始信号，两个线程同时开始执行
            
            log.info("等待线程执行完成，最多等待10秒");
            // 添加超时机制，避免无限等待
            thread1.join(10000); // 10秒超时
            thread2.join(10000); // 10秒超时
            log.info("线程join等待结束");

            // 检查线程是否还在运行（增加循环检查机制）
            long checkStartTime = System.currentTimeMillis();
            boolean thread1Finished = !thread1.isAlive();
            boolean thread2Finished = !thread2.isAlive();
            
            log.info("开始检查线程状态: thread1 alive={}, thread2 alive={}", 
                thread1.isAlive(), thread2.isAlive());
            
            // 在1秒内循环检查线程状态
            while (System.currentTimeMillis() - checkStartTime < 1000) {
                if (!thread1Finished && !thread1.isAlive()) {
                    log.info("检测到线程1已完成执行");
                    thread1Finished = true;
                }
                if (!thread2Finished && !thread2.isAlive()) {
                    log.info("检测到线程2已完成执行");
                    thread2Finished = true;
                }
                
                // 如果两个线程都已完成，直接退出
                if (thread1Finished && thread2Finished) {
                    log.info("两个线程都已完成执行");
                    break;
                }
                
                // 短暂休眠，避免过度占用CPU
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            // 最终检查，如果线程仍然存活则认为可能发生了死锁
            log.info("最终线程状态检查: thread1 alive={}, thread2 alive={}", 
                thread1.isAlive(), thread2.isAlive());
                
            if (thread1.isAlive()) {
                log.warn("线程1执行超时，可能发生死锁，强制中断");
                thread1.interrupt();
            }
            if (thread2.isAlive()) {
                log.warn("线程2执行超时，可能发生死锁，强制中断");
                thread2.interrupt();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("主线程被中断: {}", e.getMessage());
        }

        log.info("转账死锁模拟完成");
    }
    
    /**
     * 模拟并发转账场景 - 增强版（更高死锁概率）
     */
    public void simulateDeadlockEnhanced() {
        log.info("开始模拟转账死锁场景（增强版）...");
        
        // 使用更多线程增加死锁概率
        final CountDownLatch readyLatch = new CountDownLatch(4);  // 4个线程
        final CountDownLatch startLatch = new CountDownLatch(1);
        
        // 线程1: ACC001 -> ACC002
        Thread thread1 = new Thread(() -> {
            try {
                log.info("线程1准备就绪");
                readyLatch.countDown();
                startLatch.await();
                log.info("线程1开始执行");
                self.transferWithDeadlock("ACC001", "ACC002", new BigDecimal("50.00"));
                log.info("线程1执行完成");
            } catch (Exception e) {
                log.error("转账线程1异常: {}", e.getMessage());
            }
        }, "Enhanced-Thread-1");
        
        // 线程2: ACC002 -> ACC001 
        Thread thread2 = new Thread(() -> {
            try {
                log.info("线程2准备就绪");
                readyLatch.countDown();
                startLatch.await();
                log.info("线程2开始执行");
                self.transferWithDeadlock("ACC002", "ACC001", new BigDecimal("75.00"));
                log.info("线程2执行完成");
            } catch (Exception e) {
                log.error("转账线程2异常: {}", e.getMessage());
            }
        }, "Enhanced-Thread-2");
        
        // 线程3: ACC001 -> ACC002 (同线程1方向)
        Thread thread3 = new Thread(() -> {
            try {
                log.info("线程3准备就绪");
                readyLatch.countDown();
                startLatch.await();
                log.info("线程3开始执行");
                self.transferWithDeadlock("ACC001", "ACC002", new BigDecimal("25.00"));
                log.info("线程3执行完成");
            } catch (Exception e) {
                log.error("转账线程3异常: {}", e.getMessage());
            }
        }, "Enhanced-Thread-3");
        
        // 线程4: ACC002 -> ACC001 (同线程2方向)
        Thread thread4 = new Thread(() -> {
            try {
                log.info("线程4准备就绪");
                readyLatch.countDown();
                startLatch.await();
                log.info("线程4开始执行");
                self.transferWithDeadlock("ACC002", "ACC001", new BigDecimal("30.00"));
                log.info("线程4执行完成");
            } catch (Exception e) {
                log.error("转账线程4异常: {}", e.getMessage());
            }
        }, "Enhanced-Thread-4");
        
        Thread[] threads = {thread1, thread2, thread3, thread4};
        
        log.info("启动4个线程");
        for (Thread t : threads) {
            t.start();
        }
        
        try {
            log.info("等待所有线程准备就绪...");
            readyLatch.await();
            log.info("所有线程准备就绪，发送开始信号");
            startLatch.countDown();
            
            log.info("等待线程执行完成，最多等待15秒");
            for (Thread t : threads) {
                t.join(15000);
            }
            
            // 检查是否有线程仍在运行（可能死锁）
            boolean hasDeadlock = false;
            for (int i = 0; i < threads.length; i++) {
                if (threads[i].isAlive()) {
                    log.warn("线程{}仍在运行，可能发生死锁", i + 1);
                    hasDeadlock = true;
                    threads[i].interrupt();
                }
            }
            
            if (hasDeadlock) {
                log.error("检测到死锁！已强制中断相关线程");
            } else {
                log.info("所有线程正常完成，未检测到死锁");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("主线程被中断: {}", e.getMessage());
        }
        
        log.info("增强版死锁模拟完成");
    }
}