package com.geektime.basic.generic.training.day2.before;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示：没有通配符时遇到的问题
 *
 * 运行这个类，看看为什么需要通配符
 */
public class WildcardProblemDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   没有通配符时的问题");
        System.out.println("========================================\n");

        problem1_CannotAssignSubtypeList();
        problem2_CannotPassSubtypeList();
        problem3_CodeDuplication();

        System.out.println("\n========================================");
        System.out.println("💔 核心问题：");
        System.out.println("----------------------------------------");
        System.out.println("1. List<Dog> 不能赋值给 List<Animal>");
        System.out.println("2. 接受 List<Animal> 的方法不能传入 List<Dog>");
        System.out.println("3. 需要为每个子类重载方法，代码重复");
        System.out.println("\n🎯 解决方案：通配符 ? extends 和 ? super");
        System.out.println("========================================");
    }

    /**
     * 问题1：不能把子类型的List赋值给父类型的List
     */
    private static void problem1_CannotAssignSubtypeList() {
        System.out.println("【问题1】不能把 List<Dog> 赋值给 List<Animal>");
        System.out.println("----------------------------------------");

        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("旺财"));
        dogs.add(new Dog("小黑"));

        // ❌ 编译错误：不能把 List<Dog> 赋值给 List<Animal>
//         List<Animal> animals = dogs;
        // 错误信息：Incompatible types: List<Dog> cannot be converted to List<Animal>

        System.out.println("✗ List<Animal> animals = dogs; // 编译错误！");
        System.out.println("✗ Dog是Animal的子类，但List<Dog>不是List<Animal>的子类");
        System.out.println();
    }

    /**
     * 问题2：不能把子类型的List传给接受父类型List的方法
     */
    private static void problem2_CannotPassSubtypeList() {
        System.out.println("【问题2】不能把 List<Dog> 传给接受 List<Animal> 的方法");
        System.out.println("----------------------------------------");

        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("旺财"));
        dogs.add(new Dog("小黑"));

        List<Cat> cats = new ArrayList<>();
        cats.add(new Cat("咪咪"));
        cats.add(new Cat("喵喵"));

        // ❌ 编译错误：不能传入 List<Dog>
//         printAnimals(dogs);
        // 错误信息：printAnimals(List<Animal>) cannot be applied to List<Dog>

        System.out.println("✗ printAnimals(dogs); // 编译错误！");
        System.out.println("✗ printAnimals(cats); // 编译错误！");
        System.out.println("✗ 只能传入 List<Animal>，不能传入子类的List");
        System.out.println();
    }

    /**
     * ❌ 这个方法只能接受 List<Animal>，不能接受 List<Dog> 或 List<Cat>
     */
    private static void printAnimals(List<Animal> animals) {
        System.out.println("打印动物列表：");
        for (Animal animal : animals) {
            animal.makeSound();
        }
    }

    /**
     * 问题3：为了支持不同类型，需要重载多个方法（代码重复）
     */
    private static void problem3_CodeDuplication() {
        System.out.println("【问题3】需要重载多个方法，代码重复");
        System.out.println("----------------------------------------");

        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("旺财"));

        List<Cat> cats = new ArrayList<>();
        cats.add(new Cat("咪咪"));

        List<Animal> animals = new ArrayList<>();
        animals.add(new Animal("未知动物"));

        // 😢 需要写3个几乎一样的方法
        printDogs(dogs);
        printCats(cats);
        printAnimalsExact(animals);

        System.out.println();
        System.out.println("💔 需要为每个类型写一个方法，代码重复！");
        System.out.println("💔 如果新增一个 Bird 类，又要再写一个 printBirds()");
    }

    /**
     * ❌ 重复代码1：打印狗列表
     */
    private static void printDogs(List<Dog> dogs) {
        System.out.println("打印狗列表：");
        for (Dog dog : dogs) {
            System.out.println("  - " + dog);
        }
    }

    /**
     * ❌ 重复代码2：打印猫列表
     */
    private static void printCats(List<Cat> cats) {
        System.out.println("打印猫列表：");
        for (Cat cat : cats) {
            System.out.println("  - " + cat);
        }
    }

    /**
     * ❌ 重复代码3：打印动物列表
     */
    private static void printAnimalsExact(List<Animal> animals) {
        System.out.println("打印动物列表：");
        for (Animal animal : animals) {
            System.out.println("  - " + animal);
        }
    }
}
