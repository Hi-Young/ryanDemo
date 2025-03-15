package com.geektime.jvm.classLoader;

public class Test {
    public static void say() {
        System.out.println("Hello CurrentHeroClassLoader");
    }

    public static void main(String[] args) {
        try {
            say();
//            Thread.sleep(1000000000);
        } catch (Exception e) {


        }

    }
}
