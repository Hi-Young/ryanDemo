package com.geektime.basic.generic.training.day2.before;

/**
 * 动物基类
 */
public class Animal {
    private String name;

    public Animal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void makeSound() {
        System.out.println(name + " makes a sound");
    }

    @Override
    public String toString() {
        return "Animal{name='" + name + "'}";
    }
}
