package com.example.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.example.demo.service.UserService.addUser(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Before method:" + joinPoint.getSignature().getName());
        System.out.println("Arguments:" + joinPoint.getArgs()[0]);
    }

    @After("execution(* com.example.demo.service.UserService.addUser(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("After method:" + joinPoint.getSignature().getName());
    }

}
