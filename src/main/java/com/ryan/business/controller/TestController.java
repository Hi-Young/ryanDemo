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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
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
//    @Transactional(rollbackFor = Exception.class, isolation =
//            Isolation.REPEATABLE_READ)
    @Transactional(rollbackFor = Exception.class)
    public List<User> test() {
        List<User> list = null;
        for (int i = 1; i < 3; i++) {
            list = userMapper.listAllDataPage(i*10);
            log.info("list.size:{} is:{}", list.size(), list);
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

    @PostMapping("/genericTest")
    public void genericTest() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        try {
            list.getClass().getMethod("add", Object.class).invoke(list, "asd");
        } catch (IllegalAccessException e) {
            
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if( arrayList instanceof ArrayList){}
    }

    public static <T> T add(T x, T y) {
        return y;
    }
    
}


