package com.ryan.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.business.entity.user.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<User> queryAllData();
    
    User getUserDetail(Long id);

    List<User> listAllDataPage(@Param("size") Integer size);
    
}


