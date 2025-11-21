package com.geektime.basic.generic.training.day2.before;

/**
 * 猫（继承自Animal）
 */
public class Cat extends Animal {

    public Cat(String name) {
        super(name);
    }

    @Override
    public void makeSound() {
        System.out.println(getName() + " meows: Meow!");
    }

    @Override
    public String toString() {
        return "Cat{name='" + getName() + "'}";
    }
}
