package com.ryan.business.controller;

import com.ryan.business.entity.user.UserDTO;
import com.ryan.common.base.ResultVO;
import com.ryan.common.exceptionhandler.BusinessException;
import com.ryan.common.exceptionhandler.ParamException;
import com.ryan.common.exceptionhandler.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/exception")
@Slf4j
public class ExceptionTestController {

    /**
     * 正常接口 - 测试成功返回
     * GET /api/exception/success
     */
    @GetMapping("/success")
    public ResultVO<String> success() {
        return ResultVO.success("操作成功！");
    }

    /**
     * 业务异常测试
     * GET /api/exception/business
     */
    @GetMapping("/business")
    public ResultVO<String> businessException() {
        // 模拟业务逻辑异常
        if (System.currentTimeMillis() > 0) {
            throw new BusinessException(4001, "用户余额不足，无法完成操作");
        }
        return ResultVO.success();
    }

    /**
     * 参数异常测试
     * GET /api/exception/param?age=abc
     */
    @GetMapping("/param")
    public ResultVO<String> paramException(@RequestParam String age) {
        try {
            int userAge = Integer.parseInt(age);
            if (userAge < 0) {
                throw new ParamException("年龄不能为负数");
            }
        } catch (NumberFormatException e) {
            throw new ParamException("年龄必须是有效的数字");
        }
        return ResultVO.success("参数验证通过");
    }

    /**
     * 资源不存在异常测试
     * GET /api/exception/notfound/{id}
     */
    @GetMapping("/notfound/{id}")
    public ResultVO<String> resourceNotFound(@PathVariable Long id) {
        // 模拟查询用户
        if (id <= 0 || id > 1000) {
            throw new ResourceNotFoundException("用户ID: " + id + " 不存在");
        }
        return ResultVO.success("用户信息查询成功");
    }

    /**
     * 空指针异常测试
     * GET /api/exception/nullpointer
     */
    @GetMapping("/nullpointer")
    public ResultVO<String> nullPointerException() {
        String str = null;
        // 故意触发空指针异常
        int length = str.length();
        return ResultVO.success("长度: " + length);
    }

    /**
     * 数组越界异常测试
     * GET /api/exception/arrayindex
     */
    @GetMapping("/arrayindex")
    public ResultVO<String> arrayIndexException() {
        int[] array = {1, 2, 3};
        // 故意访问超出数组长度的索引
        int value = array[10];
        return ResultVO.success("值: " + value);
    }

    /**
     * 除零异常测试
     * GET /api/exception/divide
     */
    @GetMapping("/divide")
    public ResultVO<String> divideByZeroException() {
        int a = 10;
        int b = 0;
        // 故意除零
        int result = a / b;
        return ResultVO.success("结果: " + result);
    }

    /**
     * SQL异常模拟测试
     * GET /api/exception/sql
     */
    @GetMapping("/sql")
    public ResultVO<String> sqlException() throws SQLException {
        // 模拟SQL异常
        throw new SQLException("数据库连接超时", "08001", 1040);
    }

    /**
     * 类型转换异常测试
     * GET /api/exception/classcast
     */
    @GetMapping("/classcast")
    public ResultVO<String> classCastException() {
        Object obj = "这是一个字符串";
        // 故意进行错误的类型转换
        Integer number = (Integer) obj;
        return ResultVO.success("转换结果: " + number);
    }

    /**
     * 参数校验异常测试 (需要添加@Valid注解和DTO)
     * POST /api/exception/validation
     * Body: {"name": "", "age": -1, "email": "invalid-email"}
     */
    @PostMapping("/validation")
    public ResultVO<String> validationException(@Valid @RequestBody UserDTO userDTO) {
        return ResultVO.success("用户信息验证通过: " + userDTO.getName());
    }

    /**
     * 一般异常测试
     * GET /api/exception/general
     */
    @GetMapping("/general")
    public ResultVO<String> generalException() throws Exception {
        // 抛出一般异常
        throw new Exception("这是一个一般性异常，用于测试兜底处理");
    }

    /**
     * 运行时异常测试
     * GET /api/exception/runtime
     */
    @GetMapping("/runtime")
    public ResultVO<String> runtimeException() {
        throw new RuntimeException("这是一个运行时异常");
    }
}


