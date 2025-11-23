package com.geektime.basic.generic.training.day2.practice.answer;

import com.geektime.basic.generic.training.day2.before.Animal;
import com.geektime.basic.generic.training.day2.before.Cat;
import com.geektime.basic.generic.training.day2.before.Dog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 参考答案：通配符填空练习
 *
 * ⚠️ 先自己思考，再看这个答案！
 */
public class WildcardFillInBlanksAnswer {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   通配符填空练习 - 参考答案");
        System.out.println("========================================\n");

        test1_PrintAnimals();
        test2_AddDog();
        test3_CopyList();
        test4_FindMax();
        test5_AddAll();
        test6_SwapElements();

        System.out.println("\n========================================");
        System.out.println("🎉 所有测试通过！");
        System.out.println("========================================");
    }

    private static void test1_PrintAnimals() {
        System.out.println("【练习1】打印动物列表");
        System.out.println("----------------------------------------");

        List<Dog> dogs = Arrays.asList(new Dog("旺财"), new Dog("小黑"));
        List<Cat> cats = Arrays.asList(new Cat("咪咪"), new Cat("喵喵"));

        printAnimals(dogs);
        printAnimals(cats);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案1：List<? extends Animal>
     *
     * 原因：
     * - 需要从列表中**读取**数据 ✅
     * - 不需要往列表中**写入**数据 ❌
     * - 只读 → 用 ? extends
     *
     * PECS：Producer Extends（生产者用extends）
     */
    private static void printAnimals(List<? extends Animal> animals) {
        for (Animal animal : animals) {
            System.out.println("  - " + animal);
        }
    }

    private static void test2_AddDog() {
        System.out.println("【练习2】添加狗到列表");
        System.out.println("----------------------------------------");

        List<Dog> dogList = new ArrayList<>();
        List<Animal> animalList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        Dog dog = new Dog("旺财");

        addDog(dogList, dog);
        addDog(animalList, dog);
        addDog(objectList, dog);

        System.out.println("✓ 已添加到 List<Dog>: " + dogList);
        System.out.println("✓ 已添加到 List<Animal>: " + animalList);
        System.out.println("✓ 已添加到 List<Object>: " + objectList);
        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案2：List<? super Dog>
     *
     * 原因：
     * - 不需要从列表中**读取**数据 ❌
     * - 需要往列表中**写入**数据 ✅
     * - 只写 → 用 ? super
     *
     * PECS：Consumer Super（消费者用super）
     */
    private static void addDog(List<? super Dog> list, Dog dog) {
        list.add(dog);
    }

    private static void test3_CopyList() {
        System.out.println("【练习3】复制列表");
        System.out.println("----------------------------------------");

        List<Dog> dogs = Arrays.asList(new Dog("旺财"), new Dog("小黑"));
        List<Animal> animals = new ArrayList<>();

        copyList(dogs, animals);

        System.out.println("✓ 从 List<Dog> 复制到 List<Animal>: " + animals);
        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案3：
     * - src: List<? extends T>（读取，生产者）
     * - dest: List<? super T>（写入，消费者）
     *
     * 原因：
     * - src 需要读取 → 用 ? extends
     * - dest 需要写入 → 用 ? super
     *
     * 这是PECS原则的经典应用！
     */
    private static <T> void copyList(
        List<? extends T> src,    // 读取（生产者）
        List<? super T> dest      // 写入（消费者）
    ) {
        for (T item : src) {
            dest.add(item);
        }
    }

    private static void test4_FindMax() {
        System.out.println("【练习4】找最大值");
        System.out.println("----------------------------------------");

        List<Integer> integers = Arrays.asList(1, 5, 3, 9, 2);
        Integer max = findMax(integers);

        System.out.println("✓ 最大值: " + max);
        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案4：List<? extends T>
     *
     * 原因：
     * - 需要从列表中**读取**数据 ✅
     * - 不需要往列表中**写入**数据 ❌
     * - 只读 → 用 ? extends
     */
    private static <T extends Comparable<T>> T findMax(List<? extends T> list) {
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    private static void test5_AddAll() {
        System.out.println("【练习5】批量添加");
        System.out.println("----------------------------------------");

        List<Integer> src = Arrays.asList(1, 2, 3);
        List<Number> dest = new ArrayList<>();

        addAll(dest, src);

        System.out.println("✓ 已添加到目标列表: " + dest);
        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案5：
     * - dest: List<? super T>（写入，消费者）
     * - src: List<? extends T>（读取，生产者）
     *
     * 原因：
     * - dest 需要写入 → 用 ? super
     * - src 需要读取 → 用 ? extends
     */
    private static <T> void addAll(
        List<? super T> dest,     // 写入（消费者）
        List<? extends T> src     // 读取（生产者）
    ) {
        for (T item : src) {
            dest.add(item);
        }
    }

    private static void test6_SwapElements() {
        System.out.println("【练习6】交换元素（挑战）");
        System.out.println("----------------------------------------");

        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        System.out.println("交换前: " + numbers);

        swap(numbers, 0, 4);
        System.out.println("交换后: " + numbers);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案6：List<?> 或者不用通配符直接用 <T> List<T>
     *
     * 原因：
     * - 需要从列表中**读取**数据 ✅（get）
     * - 需要往列表中**写入**数据 ✅（set）
     * - 既读又写 → 不能用 ? extends 或 ? super
     * - 应该用 List<?> 配合 helper 方法
     *
     * 注意：这是个特殊情况！
     * - ? extends 不能写
     * - ? super 不能读（只能读为Object）
     * - 所以用 List<?> 配合通配符捕获技巧
     */
    private static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }

    /**
     * 通配符捕获（Wildcard Capture）
     * 通过泛型方法捕获具体类型
     */
    private static <T> void swapHelper(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
