package com.ryan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.study.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {


    List<User> queryAllData();
    
    
}
