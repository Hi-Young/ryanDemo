package com.ryan.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.business.entity.user.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {


    List<User> queryAllData();
    
    
}


