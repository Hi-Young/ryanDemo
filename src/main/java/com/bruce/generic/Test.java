package com.bruce.generic;

public class Test {

    public static void main(String[] args) {
        Cat instance = GetInstanceClass.getInstance(Cat.class);
        instance.setName("miao");
        System.out.println(instance);
    }
}
