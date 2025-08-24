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
import java.util.concurrent.TimeUnit;

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
        log.info("开始转账: {} -> {}, 金额: {}", fromAccountNo, toAccountNo, amount);
        
        // 先获取转出账户锁 (可能导致死锁的关键点1)
        Account fromAccount = accountMapper.selectByAccountNoForUpdate(fromAccountNo);
        if (fromAccount == null) {
            throw new RuntimeException("转出账户不存在: " + fromAccountNo);
        }
        
        // 模拟一些处理时间，增加死锁概率
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 再获取转入账户锁 (可能导致死锁的关键点2)
        Account toAccount = accountMapper.selectByAccountNoForUpdate(toAccountNo);
        if (toAccount == null) {
            throw new RuntimeException("转入账户不存在: " + toAccountNo);
        }
        
        // 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("账户余额不足: " + fromAccount.getBalance());
        }
        
        // 执行转账
        accountMapper.updateBalanceByAccountNo(fromAccountNo, amount.negate());
        accountMapper.updateBalanceByAccountNo(toAccountNo, amount);
        
        log.info("转账完成: {} -> {}, 金额: {}", fromAccountNo, toAccountNo, amount);
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
        
        Thread thread1 = new Thread(() -> {
            try {
                transferWithDeadlock("ACC001", "ACC002", new BigDecimal("100.00"));
            } catch (Exception e) {
                log.error("转账线程1异常: {}", e.getMessage());
            }
        }, "Transfer-Thread-1");
        
        Thread thread2 = new Thread(() -> {
            try {
                // 故意颠倒顺序，制造死锁
                transferWithDeadlock("ACC002", "ACC001", new BigDecimal("200.00"));
            } catch (Exception e) {
                log.error("转账线程2异常: {}", e.getMessage());
            }
        }, "Transfer-Thread-2");
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("转账死锁模拟完成");
    }
}