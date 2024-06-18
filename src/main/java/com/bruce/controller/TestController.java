package com.bruce.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author heyyon
 * @date 2024-04-16 7:45
 */
@RestController
@RequestMapping("/testController")
public class TestController {


    @PostMapping("/test")
    public List<String> test() {
        return Arrays.asList("1","2","3");
    }

    @PostMapping("/testInteger")
    public void testInteger() {
        Integer integer = Integer.valueOf(123);
        Integer integer1 = new Integer(123);
    }
}
