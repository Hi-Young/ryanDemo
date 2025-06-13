package com.ryan.business.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.business.user.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {


    List<User> queryAllData();
    
    
}


