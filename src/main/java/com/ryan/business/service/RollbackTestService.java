package com.ryan.business.service;

import io.netty.handler.codec.http.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ryan.business.mapper.UserMapper;

@Service
public class RollbackTestService {

    @Autowired
    private InnerTransactionService innerTransactionService;
    
    @Autowired
    private UserMapper userMapper;

    @Transactional
    public void outerTransaction(boolean triggerError) throws Exception {
        System.out.println("外层事务开始");
        
        // 外层先修改数据
        userMapper.updateAge(1, 100);
        System.out.println("外层事务：将用户ID=1的年龄改为100");
        
            try {
                // 调用另一个Service的事务方法 - 通过Spring代理调用
                innerTransactionService.doBusinessLogic();
            } catch (RuntimeException e) {
                System.out.println("外层捕获到异常: " + e.getMessage());
                // 关键：内层方法抛异常时，事务被标记为rollback-only
                // 但外层catch了异常，方法正常结束
                // Spring提交时发现rollback-only状态，抛出UnexpectedRollbackException
                // 由于rollback-only，外层的数据修改也会被回滚
            }
        System.out.println("外层事务正常结束");
    }
    
    public void doBusinessLogic() throws Exception {
        System.out.println("内层事务方法执行");

        // 内层也修改数据
        userMapper.updateAge(2, 200);
        System.out.println("内层事务：将用户ID=2的年龄改为200");

        System.out.println("内层抛出RuntimeException");
        throw new RuntimeException("内层业务异常");
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkedExceptionWithRollback() throws Exception {
        System.out.println("事务开始");

        try {
            // 直接在当前事务中抛出异常
            System.out.println("抛出受检异常");
            throw new Exception("这是一个受检异常");
        } catch (Exception e) {
            System.out.println("捕获到异常: " + e.getMessage());
            // 事务已经被标记为 rollback-only，但我们捕获了异常
            // 当事务尝试提交时会报错
        }

        System.out.println("事务方法继续执行...");
        // 这里会触发 rollback-only 异常
    }

    private void methodThatThrowsCheckedException() throws Exception {
        System.out.println("抛出受检异常");
        throw new Exception("这是一个受检异常");
    }
}