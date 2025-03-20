package com.ryan.study.designpattern;

import org.springframework.stereotype.Component;

@Component
public class UserDao implements IUserDao{

    @Override
    public void save() {
        System.out.println("保存数据");
    }

    @Override
    public void insert() {
        System.out.println("插入数据");
    }


}
