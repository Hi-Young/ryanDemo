package com.geektime.basic.generic.training.day2.before;

/**
 * 狗（继承自Animal）
 */
public class Dog extends Animal {

    public Dog(String name) {
        super(name);
    }

    @Override
    public void makeSound() {
        System.out.println(getName() + " barks: Woof!");
    }

    @Override
    public String toString() {
        return "Dog{name='" + getName() + "'}";
    }
}
