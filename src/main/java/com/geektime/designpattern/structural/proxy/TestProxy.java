package com.geektime.designpattern.structural.proxy;

import org.junit.Test;

public class TestProxy {

    @Test
    public void testDynamicProxy (){
        IUserDao target = new UserDao();
        System.out.println(target.getClass());  //输出目标对象信息
        IUserDao proxy = (IUserDao) new ProxyFactory(target).getProxyInstance();
        System.out.println(proxy.getClass());  //输出代理对象信息
        proxy.insert();  //执行代理方法
    }
    
    
}

