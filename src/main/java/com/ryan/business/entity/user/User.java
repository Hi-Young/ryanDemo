package com.ryan.business.entity.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    //主键
    private Long id;
    //用户名
    private String name;
    //邮箱
    private String email;
    //年龄
    private Integer age;
    //直属上级
    private Long managerId;
    //创建时间
    private LocalDateTime createTime;
}