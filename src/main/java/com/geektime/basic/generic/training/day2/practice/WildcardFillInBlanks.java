package com.geektime.basic.generic.training.day2.practice;

import com.geektime.basic.generic.training.day2.before.Animal;
import com.geektime.basic.generic.training.day2.before.Cat;
import com.geektime.basic.generic.training.day2.before.Dog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通配符填空练习
 *
 * 🎯 核心任务：判断每个方法应该用什么通配符
 *
 * 你需要填写：
 * - ? extends T（上界通配符，只读）
 * - ? super T（下界通配符，只写）
 * - T（普通泛型参数）
 *
 * 💡 提示：
 * - 如果方法需要**读取**列表数据 → 用 ? extends
 * - 如果方法需要**写入**列表数据 → 用 ? super
 * - 如果既要读又要写 → 用 T
 */
public class WildcardFillInBlanks {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   通配符填空练习");
        System.out.println("========================================\n");

//        test1_PrintAnimals();
//        test2_AddDog();
        test3_CopyList();
//        test4_FindMax();
//        test5_AddAll();
//        test6_SwapElements();

        System.out.println("\n========================================");
        System.out.println("🎉 所有测试通过！");
        System.out.println("========================================");
    }

    // ========================================
    // 练习1：打印动物列表
    // ========================================
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
     * 🎯 TODO 1: 填写通配符
     *
     * 需求：这个方法需要打印动物列表
     * 分析：
     * - 需要从列表中**读取**数据吗？ 是/否
     * - 需要往列表中**写入**数据吗？ 是/否
     *
     * 问题：应该填什么？
     * A. List<Animal>
     * B. List<? extends Animal>
     * C. List<? super Animal>
     * D. List<T>
     *
     * 答案：_____（在下面填写）
     */
    private static void printAnimals(List<? extends Animal> animals) {
        // 方法体已实现，你只需要填写上面的通配符
        for (Animal animal : animals) {
            System.out.println("  - " + animal);
        }
    }

    // ========================================
    // 练习2：添加狗到列表
    // ========================================
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
     * 🎯 TODO 2: 填写通配符
     *
     * 需求：把一只狗添加到列表中
     * 分析：
     * - 需要从列表中**读取**数据吗？ 是/否
     * - 需要往列表中**写入**数据吗？ 是/否
     *
     * 问题：应该填什么？
     * A. List<Dog>
     * B. List<? extends Dog>
     * C. List<? super Dog>
     * D. List<T>
     *
     * 答案：_____（在下面填写）
     */
    private static void addDog(List<? super Dog> list, Dog dog) {
        // 方法体已实现
        list.add(dog);
    }

    // ========================================
    // 练习3：复制列表
    // ========================================
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
     * 🎯 TODO 3: 填写两个通配符
     *
     * 需求：从源列表复制数据到目标列表
     * 分析：
     * - src（源列表）：需要**读取**数据
     * - dest（目标列表）：需要**写入**数据
     *
     * 问题1：src 应该填什么？
     * A. List<T>
     * B. List<? extends T>
     * C. List<? super T>
     *
     * 问题2：dest 应该填什么？
     * A. List<T>
     * B. List<? extends T>
     * C. List<? super T>
     *
     * 答案：src=_____, dest=_____（在下面填写）
     */
    private static <T> void copyList(
        List<? extends T> src, List<? super T> dest) {
        // 方法体已实现
        for (T item : src) {
            dest.add(item);
        }
    }

    // ========================================
    // 练习4：找最大值
    // ========================================
    private static void test4_FindMax() {
        System.out.println("【练习4】找最大值");
        System.out.println("----------------------------------------");

        List<Integer> integers = Arrays.asList(1, 5, 3, 9, 2);
        Integer max = findMax(integers);

        System.out.println("✓ 最大值: " + max);
        System.out.println("✓ 测试通过！\n");
    }

    /**
     * 🎯 TODO 4: 填写通配符
     *
     * 需求：找出列表中的最大值
     * 分析：
     * - 需要从列表中**读取**数据吗？ 是/否
     * - 需要往列表中**写入**数据吗？ 是/否
     *
     * 问题：应该填什么？
     * A. List<T>
     * B. List<? extends T>
     * C. List<? super T>
     *
     * 答案：_____（在下面填写）
     */
    private static <T extends Comparable<T>> T findMax(List<? extends T> list) {
        // 方法体已实现
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    // ========================================
    // 练习5：批量添加
    // ========================================
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
     * 🎯 TODO 5: 填写两个通配符
     *
     * 需求：把src的所有元素添加到dest
     * 分析：
     * - src（源列表）：需要**读取**数据
     * - dest（目标列表）：需要**写入**数据
     *
     * 问题1：src 应该填什么？
     * 问题2：dest 应该填什么？
     *
     * 答案：src=_____, dest=_____（在下面填写）
     */
    private static <T> void addAll(
        List<? super T> dest,
        List<? extends T> src
    ) {
        // 方法体已实现
        for (T item : src) {
            dest.add(item);
        }
    }

    // ========================================
    // 练习6：交换元素（挑战题）
    // ========================================
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
     * 🎯 TODO 6: 填写通配符（挑战题）
     *
     * 需求：交换列表中两个位置的元素
     * 分析：
     * - 需要从列表中**读取**数据吗？ 是/否
     * - 需要往列表中**写入**数据吗？ 是/否
     *
     * 问题：应该填什么？
     * A. List<T>
     * B. List<? extends T>
     * C. List<? super T>
     * D. List<?>
     *
     * 提示：既要读又要写！
     *
     * 答案：_____（在下面填写）
     */
    private static <T> void swap(List<T> list, int i, int j) {
        // 方法体已实现
        // 注意：这里既要读取（get）又要写入（set）
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
