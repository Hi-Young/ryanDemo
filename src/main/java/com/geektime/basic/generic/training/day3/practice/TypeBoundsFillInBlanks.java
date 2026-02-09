package com.geektime.basic.generic.training.day3.practice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 类型约束填空练习
 *
 * 🎯 核心任务：判断每个方法应该用什么类型约束
 *
 * 你需要填写：
 * - <T extends Comparable<T>>（单一约束：可比较）
 * - <T extends Number>（单一约束：数字类型）
 * - <T extends A & B>（多重约束）
 * - <T>（无约束）
 *
 * 💡 判断技巧：
 * - 方法体内调用了 T 的某个方法 → 需要约束
 * - 只是存储、传递 T → 不需要约束
 */
public class TypeBoundsFillInBlanks {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   类型约束填空练习");
        System.out.println("========================================\n");

//        test1_FindMax();
//        test2_Sum();
//        test3_Sort();
//        test4_SaveAndCompare();
//        test5_ProcessNumbers();
//        test6_CopyAndSort();

        System.out.println("\n========================================");
        System.out.println("🎉 所有测试通过！");
        System.out.println("========================================");
    }

    // ========================================
    // 练习1：找最大值
    // ========================================
    private static void test1_FindMax() {
        System.out.println("【练习1】找最大值");
        System.out.println("----------------------------------------");

        List<Integer> numbers = Arrays.asList(5, 2, 9, 1, 7);
        Integer max = findMax(numbers);
        System.out.println("✓ 最大值: " + max);

        List<String> words = Arrays.asList("apple", "zebra", "banana");
        String maxWord = findMax(words);
        System.out.println("✓ 最大单词: " + maxWord);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * 🎯 TODO 1: 填写类型约束
     *
     * 需求：找出列表中的最大值
     * 分析：
     * - 方法体内调用了 item.compareTo(max)
     * - 说明 T 必须有 compareTo 方法
     * - Comparable<T> 接口提供了这个方法
     *
     * 问题：应该填什么？
     * A. <T>
     * B. <T extends Object>
     * C. <T extends Comparable<T>>
     * D. <T super Comparable<T>>
     *
     * 答案：_____（在下面填写）
     */
    private static <T extends Comparable<T>> T findMax(List<T> list) {
        // 方法体已实现
        if (list.isEmpty()) {
            throw new IllegalArgumentException("列表不能为空");
        }
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    // ========================================
    // 练习2：数值求和
    // ========================================
    private static void test2_Sum() {
        System.out.println("【练习2】数值求和");
        System.out.println("----------------------------------------");

        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        System.out.println("✓ 整数求和: " + sum(integers));

        List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5);
        System.out.println("✓ 小数求和: " + sum(doubles));

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * 🎯 TODO 2: 填写类型约束
     *
     * 需求：对数字列表求和
     * 分析：
     * - 方法体内调用了 num.doubleValue()
     * - 这个方法来自 Number 类
     * - Integer、Double、Float 等都继承自 Number
     *
     * 问题：应该填什么？
     * A. <T>
     * B. <T extends Number>
     * C. <T extends Comparable<T>>
     * D. <T extends Object>
     *
     * 答案：_____（在下面填写）
     */
    private static <T extends Number> double sum(List<T> list) {
        // 方法体已实现
        double total = 0.0;
        for (T num : list) {
            total += num.doubleValue();
        }
        return total;
    }

    // ========================================
    // 练习3：排序列表
    // ========================================
    private static void test3_Sort() {
        System.out.println("【练习3】排序列表");
        System.out.println("----------------------------------------");

        List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 2, 9, 1, 7));
        sortList(numbers);
        System.out.println("✓ 排序后: " + numbers);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * 🎯 TODO 3: 填写类型约束
     *
     * 需求：对列表进行排序
     * 分析：
     * - 方法体内调用了 a.compareTo(b)
     * - 需要 T 实现 Comparable 接口
     *
     * 问题：应该填什么？
     *
     * 答案：_____（在下面填写）
     */
    private static <T extends Comparable<T>> void sortList(List<T> list) {
        // 方法体已实现（简单的冒泡排序）
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                T a = list.get(j);
                T b = list.get(j + 1);
                if (a.compareTo(b) > 0) {
                    list.set(j, b);
                    list.set(j + 1, a);
                }
            }
        }
    }

    // ========================================
    // 练习4：保存并比较（多重约束）⭐⭐
    // ========================================
    private static void test4_SaveAndCompare() {
        System.out.println("【练习4】保存并比较（多重约束）");
        System.out.println("----------------------------------------");

        List<String> strings = Arrays.asList("hello", "world", "java");
        String max = findMaxAndSave(strings);
        System.out.println("✓ 最大值（可序列化）: " + max);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * 🎯 TODO 4: 填写类型约束（挑战题）
     *
     * 需求：找最大值，并且要能序列化保存
     * 分析：
     * - 需要调用 compareTo → 实现 Comparable<T>
     * - 需要能序列化 → 实现 Serializable
     * - 这是**多重约束**！
     *
     * 问题：应该填什么？
     * A. <T extends Comparable<T>>
     * B. <T extends Serializable>
     * C. <T extends Comparable<T> & Serializable>
     * D. <T extends Serializable & Comparable<T>>
     *
     * 答案：_____（在下面填写）
     *
     * 💡 提示：多重约束用 & 连接
     */
    private static <T extends Comparable<T> & Serializable> T findMaxAndSave(List<T> list) {
        // 方法体已实现
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        // 假设这里会序列化保存 max
        System.out.println("  - 序列化保存: " + max);
        return max;
    }

    // ========================================
    // 练习5：处理数值范围（判断是否需要约束）
    // ========================================
    private static void test5_ProcessNumbers() {
        System.out.println("【练习5】处理数值范围");
        System.out.println("----------------------------------------");

        List<Integer> numbers = Arrays.asList(10, 20, 30, 40, 50);
        printRange(numbers);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * 🎯 TODO 5: 填写类型约束
     *
     * 需求：打印数值的范围（最小值到最大值）
     * 分析：
     * - 调用了 item.compareTo()
     * - 调用了 min.doubleValue() 和 max.doubleValue()
     * - 既要 Comparable，又要 Number
     *
     * 问题：应该填什么？
     * A. <T extends Number>
     * B. <T extends Comparable<T>>
     * C. <T extends Number & Comparable<T>>
     * D. <T>
     *
     * 答案：_____（在下面填写）
     */
    private static <T extends Number & Comparable<T>> void printRange(List<T> list) {
        // 方法体已实现
        T min = list.get(0);
        T max = list.get(0);

        for (T item : list) {
            if (item.compareTo(min) < 0) {
                min = item;
            }
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }

        System.out.println("  - 范围: " + min.doubleValue() + " ~ " + max.doubleValue());
    }

    // ========================================
    // 练习6：复制并排序（综合练习）⭐⭐⭐
    // ========================================
//    private static void test6_CopyAndSort() {
//        System.out.println("【练习6】复制并排序（综合）");
//        System.out.println("----------------------------------------");
//
//        List<Integer> source = Arrays.asList(5, 2, 9, 1, 7);
//        List<Number> dest = new ArrayList<>();
//
//        copyAndSort(source, dest);
//
//        System.out.println("✓ 复制并排序后: " + dest);
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    /**
//     * 🎯 TODO 6: 填写所有类型约束（综合挑战题）
//     *
//     * 需求：从源列表复制数据到目标列表，并对目标列表排序
//     * 分析：
//     * - src 参数：需要读取，用 ? extends
//     * - dest 参数：需要写入和排序，排序需要 Comparable
//     * - T 本身：既要能比较，又要能从src读取到dest写入
//     *
//     * 问题1：T 应该填什么约束？
//     * 问题2：src 应该填什么通配符？
//     * 问题3：dest 应该填什么通配符？
//     *
//     * 答案：T=_____, src=_____, dest=_____（在下面填写）
//     */
//    private static <T extends Comparable<T>> void copyAndSort(
//        List<? extends T> src,
//        List<? super T> dest
//    ) {
//        // 方法体已实现
//        // 1. 复制数据
//        for (T item : src) {
//            dest.add(item);
//        }
//
//        // 2. 排序
//        for (int i = 0; i < dest.size() - 1; i++) {
//            for (int j = 0; j < dest.size() - 1 - i; j++) {
//                Comparable<? super T> a = (Comparable<? super T>) dest.get(j);
//                T b = (T) dest.get(j + 1);
//                if (a.compareTo(b) > 0) {
//                    Object temp = dest.get(j);
//                    dest.set(j, dest.get(j + 1));
//                    dest.set(j + 1, temp);
//                }
//            }
//        }
//    }
}
