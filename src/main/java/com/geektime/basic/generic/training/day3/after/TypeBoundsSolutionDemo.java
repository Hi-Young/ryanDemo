package com.geektime.basic.generic.training.day3.after;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示：用类型约束解决问题
 *
 * 对比 before/TypeBoundsProblemDemo.java
 */
public class TypeBoundsSolutionDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   用类型约束解决问题");
        System.out.println("========================================\n");

        solution1_CanCompare();
        solution2_NoDuplication();
        solution3_GenericSum();

        System.out.println("\n========================================");
        System.out.println("✅ 类型约束的威力：");
        System.out.println("----------------------------------------");
        System.out.println("1. 一个方法支持所有可比较类型");
        System.out.println("2. 消除代码重复");
        System.out.println("3. 类型安全 + 灵活性");
        System.out.println("========================================");
    }

    /**
     * 解决方案1：用类型约束实现通用的 findMax
     */
    private static void solution1_CanCompare() {
        System.out.println("【解决方案1】用类型约束实现通用比较");
        System.out.println("----------------------------------------");

        // ✅ 一个方法支持所有实现了 Comparable 的类型！
        List<Integer> numbers = new ArrayList<>();
        numbers.add(5);
        numbers.add(2);
        numbers.add(9);
        numbers.add(1);

        List<String> words = new ArrayList<>();
        words.add("apple");
        words.add("zebra");
        words.add("banana");

        List<Double> doubles = new ArrayList<>();
        doubles.add(3.14);
        doubles.add(2.71);
        doubles.add(9.99);

        System.out.println("✓ Integer 最大值: " + findMax(numbers));
        System.out.println("✓ String 最大值: " + findMax(words));
        System.out.println("✓ Double 最大值: " + findMax(doubles));

        System.out.println();
        System.out.println("💡 只写了一个方法，就支持了所有可比较类型！");
        System.out.println();
    }

    /**
     * ✅ 加上类型约束后，可以调用 compareTo 方法了！
     */
    private static <T extends Comparable<T>> T findMax(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("列表不能为空");
        }
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {  // ✅ 现在可以调用了！
                max = item;
            }
        }
        return max;
    }

    /**
     * 解决方案2：消除代码重复
     */
    private static void solution2_NoDuplication() {
        System.out.println("【解决方案2】消除代码重复");
        System.out.println("----------------------------------------");

        // ✅ 一个通用方法代替了 before 中的 3 个方法！
        List<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(5);
        integers.add(3);

        List<Double> doubles = new ArrayList<>();
        doubles.add(1.5);
        doubles.add(5.8);
        doubles.add(3.2);

        List<Long> longs = new ArrayList<>();
        longs.add(100L);
        longs.add(500L);
        longs.add(300L);

        System.out.println("✓ 整数求和: " + sum(integers));
        System.out.println("✓ 小数求和: " + sum(doubles));
        System.out.println("✓ 长整数求和: " + sum(longs));

        System.out.println();
        System.out.println("💡 before 中需要写 3 个方法，现在只需要 1 个！");
        System.out.println();
    }

    /**
     * ✅ 用 Number 约束，一个方法搞定所有数字类型
     */
    private static <T extends Number> double sum(List<T> list) {
        double total = 0.0;
        for (T num : list) {
            total += num.doubleValue();  // ✅ Number 类提供了这个方法
        }
        return total;
    }

    /**
     * 解决方案3：通用求和（进阶版）
     */
    private static void solution3_GenericSum() {
        System.out.println("【解决方案3】通用求和（支持求平均值）");
        System.out.println("----------------------------------------");

        List<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        numbers.add(40);
        numbers.add(50);

        double total = sum(numbers);
        double average = total / numbers.size();

        System.out.println("✓ 总和: " + total);
        System.out.println("✓ 平均值: " + average);

        System.out.println();
        System.out.println("💡 类型约束让代码既灵活又类型安全！");
        System.out.println();
    }

    // ========================================
    // 对比总结
    // ========================================
    static {
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│  Before vs After 对比                       │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  Before:                                    │");
        System.out.println("│  - findMaxInteger(List<Integer>)            │");
        System.out.println("│  - findMaxString(List<String>)              │");
        System.out.println("│  - findMaxDouble(List<Double>)              │");
        System.out.println("│  → 3 个重复方法                             │");
        System.out.println("│                                             │");
        System.out.println("│  After:                                     │");
        System.out.println("│  - <T extends Comparable<T>> T findMax(...) │");
        System.out.println("│  → 1 个通用方法                             │");
        System.out.println("└─────────────────────────────────────────────┘");
    }
}
