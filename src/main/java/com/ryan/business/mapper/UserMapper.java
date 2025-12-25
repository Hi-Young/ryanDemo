package com.ryan.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.business.entity.user.User;
import com.ryan.business.entity.user.UserChild;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<User> queryAllData();
    
    User getUserDetail(Long id);

    List<UserChild> listAllDataPage(@Param("size") Integer size);
    
    void updateAge(@Param("id") Integer id, @Param("age") Integer age);
    
}


