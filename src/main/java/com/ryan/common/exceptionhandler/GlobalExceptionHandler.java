package com.ryan.common.exceptionhandler;

import com.ryan.common.base.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.stream.Collectors;

// 全局异常处理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<String> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResultVO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(ParamException.class)
    public ResultVO<String> handleParamException(ParamException e) {
        log.error("参数异常: {}", e.getMessage());
        return ResultVO.error(400, e.getMessage());
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResultVO<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源不存在: {}", e.getMessage());
        return ResultVO.error(404, e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<String> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数校验失败: {}", e.getMessage());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResultVO.error(400, "参数校验失败: " + errorMsg);
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResultVO<String> handleSQLException(SQLException e) {
        log.error("数据库异常: {}", e.getMessage());
        return ResultVO.error(500, "数据库操作失败");
    }

    /**
     * 处理数组越界异常
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResultVO<String> handleArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException e) {
        log.error("数组越界异常: {}", e.getMessage());
        return ResultVO.error(500, "数组访问越界");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResultVO<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: {}", e.getMessage(), e);
        return ResultVO.error(500, "系统内部错误");
    }

    /**
     * 处理除零异常
     */
    @ExceptionHandler(ArithmeticException.class)
    public ResultVO<String> handleArithmeticException(ArithmeticException e) {
        log.error("算术异常: {}", e.getMessage());
        return ResultVO.error(500, "计算错误: " + e.getMessage());
    }

    /**
     * 处理类型转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    public ResultVO<String> handleClassCastException(ClassCastException e) {
        log.error("类型转换异常: {}", e.getMessage());
        return ResultVO.error(500, "数据类型转换失败");
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<String> handleException(Exception e) {
        log.error("[全局异常处理器]系统异常: {}", e.getMessage(), e);
        return ResultVO.error(500, "系统内部错误，请联系管理员");
    }
}