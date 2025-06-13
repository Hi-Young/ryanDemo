package com.ryan.common.exceptionhandler;

// 自定义资源不存在异常
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}