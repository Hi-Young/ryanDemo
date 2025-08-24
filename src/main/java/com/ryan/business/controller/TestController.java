package com.ryan.business.controller;

import com.ryan.business.mapper.UserMapper;
import com.ryan.business.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author heyyon
 * @date 2024-04-16 7:45
 */
@RestController
@RequestMapping("/testController")
@Slf4j
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/test")
    @Transactional(rollbackFor = Exception.class, isolation =
            Isolation.REPEATABLE_READ)
    public List<User> test() {
        List<User> list = null;
        for (int i = 1; i < 3; i++) {
            list = userMapper.listAllDataPage(i*10);
            log.info("list is:{}", list);
            try {
                if (i == 1) {
                    
                    Thread.sleep(60000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    @PostMapping("/testInteger")
    public void testInteger() {
        Integer integer = Integer.valueOf(123);
        Integer integer1 = new Integer(123);
    }
    
    
}


