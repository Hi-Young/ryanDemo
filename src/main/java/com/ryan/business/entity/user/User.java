package com.ryan.business.entity.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    //主键
    private Long id;
    //用户名
    private String userName;
    //邮箱
    private String email;
    //年龄
    private Integer age;
    //状态
    private Integer status;
    //创建时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;
}