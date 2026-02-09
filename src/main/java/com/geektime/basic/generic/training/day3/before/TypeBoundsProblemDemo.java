package com.geektime.basic.generic.training.day3.before;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示：没有类型约束时遇到的问题
 *
 * 运行这个类，看看为什么需要类型约束
 */
public class TypeBoundsProblemDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   没有类型约束时的问题");
        System.out.println("========================================\n");

        problem1_CannotCompare();
        problem2_CodeDuplication();
        problem3_CannotCallMethods();

        System.out.println("\n========================================");
        System.out.println("💔 核心问题：");
        System.out.println("----------------------------------------");
        System.out.println("1. 泛型T太宽泛，不知道它有哪些方法");
        System.out.println("2. 为了调用特定方法，需要重载多个版本");
        System.out.println("3. 代码重复，难以维护");
        System.out.println("\n🎯 解决方案：类型约束（Type Bounds）");
        System.out.println("========================================");
    }

    /**
     * 问题1：无法比较泛型对象
     */
    private static void problem1_CannotCompare() {
        System.out.println("【问题1】无法比较泛型对象");
        System.out.println("----------------------------------------");

        List<Integer> numbers = new ArrayList<>();
        numbers.add(5);
        numbers.add(2);
        numbers.add(9);
        numbers.add(1);

        // ❌ 如果不加约束，无法实现通用的 findMax 方法
        // Integer max = findMax(numbers);  // 编译错误！

        // 😢 只能为每种类型写一个方法
        Integer maxInt = findMaxInteger(numbers);
        System.out.println("✗ 必须为Integer写专门的方法: " + maxInt);

        System.out.println("✗ 如果要支持String、Double，还要再写两个方法！");
        System.out.println();
    }

    /**
     * ❌ 这个方法无法编译！
     * 原因：T 太宽泛，不知道它有 compareTo 方法
     */
//    private static <T> T findMax(List<T> list) {
//        T max = list.get(0);
//        for (T item : list) {
//            // ❌ 编译错误：T 没有 compareTo 方法！
//            if (item.compareTo(max) > 0) {
//                max = item;
//            }
//        }
//        return max;
//    }

    /**
     * 😢 只能为每种类型写专门的方法
     */
    private static Integer findMaxInteger(List<Integer> list) {
        Integer max = list.get(0);
        for (Integer item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    /**
     * 问题2：为了支持不同类型，需要重载多个方法
     */
    private static void problem2_CodeDuplication() {
        System.out.println("【问题2】代码重复，需要重载多个方法");
        System.out.println("----------------------------------------");

        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(5);
        integers.add(3);

        List<Double> doubles = new ArrayList<>();
        doubles.add(1.5);
        doubles.add(5.8);
        doubles.add(3.2);

        // 😢 需要写两个几乎一样的方法
        System.out.println("整数求和: " + sumIntegers(integers));
        System.out.println("小数求和: " + sumDoubles(doubles));

        System.out.println("✗ 如果还要支持Float、Long，又要再写两个方法！");
        System.out.println();
    }

    /**
     * ❌ 重复代码1：整数求和
     */
    private static Integer sumIntegers(List<Integer> list) {
        int sum = 0;
        for (Integer num : list) {
            sum += num;
        }
        return sum;
    }

    /**
     * ❌ 重复代码2：小数求和
     */
    private static Double sumDoubles(List<Double> list) {
        double sum = 0.0;
        for (Double num : list) {
            sum += num;
        }
        return sum;
    }

    /**
     * 问题3：无法调用特定方法
     */
    private static void problem3_CannotCallMethods() {
        System.out.println("【问题3】无法调用泛型对象的特定方法");
        System.out.println("----------------------------------------");

        List<String> strings = new ArrayList<>();
        strings.add("hello");
        strings.add("world");

        // ❌ 如果不加约束，无法实现这样的方法
        // printUpperCase(strings);  // 编译错误！

        System.out.println("✗ 想实现 printUpperCase(List<T> list)");
        System.out.println("✗ 但 T 太宽泛，不知道它有 toUpperCase() 方法");
        System.out.println("✗ 即使传入的是 List<String>，也无法编译！");
        System.out.println();
    }

    /**
     * ❌ 这个方法无法编译！
     * 原因：T 不一定有 toUpperCase 方法
     */
//    private static <T> void printUpperCase(List<T> list) {
//        for (T item : list) {
//            // ❌ 编译错误：T 没有 toUpperCase 方法！
//            System.out.println(item.toUpperCase());
//        }
//    }
}
