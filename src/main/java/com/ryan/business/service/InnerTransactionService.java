package com.ryan.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ryan.business.mapper.UserMapper;

@Service
public class InnerTransactionService {

    @Autowired
    private UserMapper userMapper;

//    @Transactional(propagation = Propagation.REQUIRED)
    public void doBusinessLogic() {
        System.out.println("内层事务方法执行");
        
        // 内层也修改数据
        userMapper.updateAge(2, 200);
        System.out.println("内层事务：将用户ID=2的年龄改为200");
        
        System.out.println("内层抛出RuntimeException");
        throw new RuntimeException("内层业务异常");
    }
}