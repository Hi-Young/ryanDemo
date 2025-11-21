package com.geektime.basic.generic.training.day2.answer;

import com.geektime.basic.generic.training.day2.before.Animal;
import com.geektime.basic.generic.training.day2.before.Dog;

import java.util.List;

/**
 * 参考答案：通配符解决方案
 *
 * ⚠️ 先自己实现，实在卡住了再看这个答案！
 */
public class WildcardSolutionAnswer {

    /**
     * 答案1：用 ? extends Animal 读取
     */
    private static void printAnimals(List<? extends Animal> animals) {
        System.out.println("打印动物列表：");
        for (Animal animal : animals) {
            animal.makeSound();
        }
    }

    /**
     * 答案2：用 ? super Dog 写入
     */
    private static void addDog(List<? super Dog> list, Dog dog) {
        list.add(dog);
    }

    /**
     * 答案3：extends + super 组合
     */
    private static <T> void copyAll(List<? extends T> src, List<? super T> dest) {
        for (T item : src) {
            dest.add(item);
        }
    }
}
