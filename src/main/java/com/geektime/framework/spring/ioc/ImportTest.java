package com.geektime.framework.spring.ioc;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ImportTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigB.class);
        ZooConfig bean = ctx.getBean(ZooConfig.class);
        Tiger bean1 = ctx.getBean(Tiger.class);
        System.out.println(bean);
        System.out.println(bean1);

    }
}

