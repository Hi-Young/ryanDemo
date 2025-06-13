package com.ryan.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.ryan.business.user.service.UserService.addUser(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Before method:" + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed();
        System.out.println("After method:" + joinPoint.getSignature().getName());
        return result;
    }
}