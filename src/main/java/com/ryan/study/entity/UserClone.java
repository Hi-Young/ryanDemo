package com.ryan.study.entity;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserClone {

    //主键
    private Long id;
    //用户名
    private String name;
    //邮箱
    private String email;
    //年龄
    private  Integer age;
    //直属上级
    private  Long managerId;
    //创建时间
    private LocalDateTime createTime;

    List<HobbiesClone> hobbiesList;
}
