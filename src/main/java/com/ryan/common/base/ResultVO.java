package com.ryan.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 统一返回结果封装类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO<T> {
    private Integer code;
    private String message;
    private T data;
    
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "操作成功", data);
    }
    
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "操作成功", null);
    }
    
    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message, null);
    }
    
    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(500, message, null);
    }
    
    public static <T> ResultVO<T> error(String message, T data) {
        return new ResultVO<>(500, message, data);
    }
}