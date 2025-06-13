package com.ryan.business.user.controller;

import com.ryan.study.entity.User;
import com.ryan.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/test")
    public List<User> test() {
        List<User> list = userMapper.queryAllData();
        int i = 1 / 0;
        return list;
    }

    @PostMapping("/testInteger")
    public void testInteger() {
        Integer integer = Integer.valueOf(123);
        Integer integer1 = new Integer(123);
    }
    
    
}

