package com.ryan.aop.demo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
//    @Before("execution(* com.ryan.demo.service.UserService.addUser(..))")
//    public void logBefore(JoinPoint joinPoint) {
//        System.out.println("Before method:" + joinPoint.getSignature().getName());
//        System.out.println("Arguments:" + joinPoint.getArgs()[0]);
//    }
//
//    @After("execution(* com.ryan.demo.service.UserService.addUser(..))")
//    public void logAfter(JoinPoint joinPoint) {
//        System.out.println("After method:" + joinPoint.getSignature().getName());
//    }

    @Around("execution(* com.ryan.aop.demo.service.UserService.addUser(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Before method:" + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed();

        System.out.println("Before after:" + joinPoint.getSignature().getName());

        return result;
    }

}
