package com.geektime.jvm.classLoader;

import java.lang.reflect.Method;

public class TestMyClassLoader {
    public static void main(String[] args) throws Exception {
        //自定义类加载器的加载路径
        HeroClassLoader hClassLoader = new HeroClassLoader("D:\\lib");
        //包名+类名
        // 当前module中虽然有个Test类，但是路径是com.geektime.jvm.classLoader.Test
        Class c = hClassLoader.loadClass("com.ryan.jvm.classLoader.Test");
        if (c != null) {
            Object obj = c.newInstance();
            Method method = c.getMethod("say", null);
            method.invoke(obj, null);
            System.out.println(c.getClassLoader().toString());
        }
    }
}
