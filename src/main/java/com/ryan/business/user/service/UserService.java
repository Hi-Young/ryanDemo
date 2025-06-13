package com.ryan.business.user.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public void addUser(String userName) {
        System.out.println("addUser:" + userName);
    }
}

