package com.ryan.common.exceptionhandler;

// 自定义参数异常
public class ParamException extends RuntimeException {
    public ParamException(String message) {
        super(message);
    }
}