package com.geektime.basic.generic.training.day2.after;

import com.geektime.basic.generic.training.day2.before.Animal;
import com.geektime.basic.generic.training.day2.before.Cat;
import com.geektime.basic.generic.training.day2.before.Dog;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示：用通配符解决问题
 *
 * 🎯 你的任务：完成下面的方法实现
 */
public class WildcardSolutionDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   用通配符解决问题");
        System.out.println("========================================\n");

        solution1_ReadFromList();
        solution2_WriteToList();
        solution3_CopyData();

        System.out.println("\n========================================");
        System.out.println("✅ 通配符的威力：");
        System.out.println("----------------------------------------");
        System.out.println("1. ? extends T：可以读取，不能写入（生产者）");
        System.out.println("2. ? super T：可以写入，不能读取（消费者）");
        System.out.println("3. PECS原则：Producer Extends, Consumer Super");
        System.out.println("========================================");
    }

    /**
     * 解决方案1：用 ? extends 读取数据（生产者）
     */
    private static void solution1_ReadFromList() {
        System.out.println("【解决方案1】用 ? extends Animal 读取数据");
        System.out.println("----------------------------------------");

        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("旺财"));
        dogs.add(new Dog("小黑"));

        List<Cat> cats = new ArrayList<>();
        cats.add(new Cat("咪咪"));
        cats.add(new Cat("喵喵"));

        List<Animal> animals = new ArrayList<>();
        animals.add(new Animal("未知动物"));

        // ✅ 现在可以传入任何 Animal 的子类List了！
        printAnimals(dogs);
        printAnimals(cats);
        printAnimals(animals);

        System.out.println("✓ 一个方法支持所有Animal及其子类的List！");
        System.out.println();
    }

    /**
     * ✅ 用通配符：? extends Animal
     *
     * 🎯 TODO: 实现这个方法
     * 提示：参数类型改为 List<? extends Animal>
     */
    private static void printAnimals(List<? extends Animal> animals) {
        for (Animal animal : animals) {
            animal.makeSound();
        }
        // TODO: 实现打印逻辑
        // 1. 遍历 animals 列表
        // 2. 对每个 animal 调用 makeSound() 方法
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 解决方案2：用 ? super 写入数据（消费者）
     */
    private static void solution2_WriteToList() {
        System.out.println("【解决方案2】用 ? super Dog 写入数据");
        System.out.println("----------------------------------------");

        List<Dog> dogList = new ArrayList<>();
        List<Animal> animalList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        Dog dog = new Dog("旺财");

        // ✅ 现在可以往 Dog 及其父类的List中添加Dog了！
        addDog(dogList, dog);
        addDog(animalList, dog);
        addDog(objectList, dog);

        System.out.println("✓ 已添加到 List<Dog>: " + dogList);
        System.out.println("✓ 已添加到 List<Animal>: " + animalList);
        System.out.println("✓ 已添加到 List<Object>: " + objectList);
        System.out.println();
    }

    /**
     * ✅ 用通配符：? super Dog
     *
     * 🎯 TODO: 实现这个方法
     * 提示：参数类型改为 List<? super Dog>
     */
    private static void addDog(List<? super Dog> list, Dog dog) {
        list.add(dog);
        // TODO: 实现添加逻辑
        // 把 dog 添加到 list 中
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 解决方案3：综合运用 extends 和 super（经典场景）
     */
    private static void solution3_CopyData() {
        System.out.println("【解决方案3】数据复制：extends + super 组合");
        System.out.println("----------------------------------------");

        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("旺财"));
        dogs.add(new Dog("小黑"));

        List<Animal> animals = new ArrayList<>();

        // ✅ 把 List<Dog> 复制到 List<Animal>
        copyAll(dogs, animals);

        System.out.println("✓ 从 List<Dog> 复制到 List<Animal>: " + animals);
        System.out.println();
    }

    /**
     * ✅ 数据复制：从源列表复制到目标列表
     *
     * 🎯 TODO: 完善方法签名和实现
     *
     * 提示：
     * 1. 源列表（src）是生产者，用 ? extends T
     * 2. 目标列表（dest）是消费者，用 ? super T
     * 3. 方法签名：<T> void copyAll(List<? extends T> src, List<? super T> dest)
     */
    private static <T> void copyAll(List<? extends T> src, List<? super T> dest) {
        dest.addAll(src);
        // TODO: 实现复制逻辑
        // 遍历 src，把每个元素添加到 dest
//        throw new UnsupportedOperationException("请实现这个方法");
    }
}
