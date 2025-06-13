package com.ryan.business.user.entity;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

// 用于参数校验测试的DTO
@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空")
    private String name;
    
    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}

