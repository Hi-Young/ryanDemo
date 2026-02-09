package com.geektime.basic.generic.training.day3.practice.answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 参考答案：类型约束填空练习
 *
 * ⚠️ 先自己思考，再看这个答案！
 */
public class TypeBoundsFillInBlanksAnswer {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   类型约束填空练习 - 参考答案");
        System.out.println("========================================\n");

        test1_FindMax();
        test2_Sum();
        test3_Sort();
        test4_SaveAndCompare();
        test5_ProcessNumbers();
//        test6_CopyAndSort();

        System.out.println("\n========================================");
        System.out.println("🎉 所有测试通过！");
        System.out.println("========================================");
    }

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
     * ✅ 答案1：<T extends Comparable<T>>
     *
     * 原因：
     * - 方法体内调用了 item.compareTo(max)
     * - compareTo 方法来自 Comparable<T> 接口
     * - 所以 T 必须实现 Comparable<T>
     *
     * 语法解释：
     * - T extends Comparable<T> 表示 T 必须实现 Comparable<T> 接口
     * - extends 用于接口和类的约束（不是 implements）
     * - Integer、String、Double 等都实现了 Comparable
     */
    private static <T extends Comparable<T>> T findMax(List<T> list) {
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
     * ✅ 答案2：<T extends Number>
     *
     * 原因：
     * - 方法体内调用了 num.doubleValue()
     * - doubleValue() 方法来自 Number 抽象类
     * - Integer、Double、Float、Long 等都继承自 Number
     *
     * 适用场景：
     * - 需要对数字进行运算
     * - 需要调用 intValue()、doubleValue() 等方法
     */
    private static <T extends Number> double sum(List<T> list) {
        double total = 0.0;
        for (T num : list) {
            total += num.doubleValue();
        }
        return total;
    }

    private static void test3_Sort() {
        System.out.println("【练习3】排序列表");
        System.out.println("----------------------------------------");

        List<Integer> numbers = new ArrayList<>(Arrays.asList(5, 2, 9, 1, 7));
        sortList(numbers);
        System.out.println("✓ 排序后: " + numbers);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案3：<T extends Comparable<T>>
     *
     * 原因：
     * - 排序需要比较元素大小
     * - 调用了 a.compareTo(b)
     * - 所以需要 Comparable<T> 约束
     *
     * 注意：这和练习1是同样的约束，只是应用场景不同
     */
    private static <T extends Comparable<T>> void sortList(List<T> list) {
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

    private static void test4_SaveAndCompare() {
        System.out.println("【练习4】保存并比较（多重约束）");
        System.out.println("----------------------------------------");

        List<String> strings = Arrays.asList("hello", "world", "java");
        String max = findMaxAndSave(strings);
        System.out.println("✓ 最大值（可序列化）: " + max);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案4：<T extends Comparable<T> & Serializable>
     *
     * 原因：
     * - 需要比较 → Comparable<T>
     * - 需要序列化 → Serializable
     * - 同时需要两个约束 → 用 & 连接
     *
     * 多重约束语法：
     * - <T extends A & B & C>
     * - 可以有多个接口
     * - 如果有类约束，类必须写在最前面：<T extends Animal & Comparable<T>>
     * - 接口顺序无所谓
     *
     * ⚠️ 注意：
     * - 不能写成 <T extends Serializable & Comparable<T>> 也可以！
     * - 接口之间顺序不重要，但如果有类，类必须第一个
     */
    private static <T extends Comparable<T> & Serializable> T findMaxAndSave(List<T> list) {
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

    private static void test5_ProcessNumbers() {
        System.out.println("【练习5】处理数值范围");
        System.out.println("----------------------------------------");

        List<Integer> numbers = Arrays.asList(10, 20, 30, 40, 50);
        printRange(numbers);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案5：<T extends Number & Comparable<T>>
     *
     * 原因：
     * - 调用了 item.compareTo() → 需要 Comparable<T>
     * - 调用了 min.doubleValue() → 需要 Number
     * - 同时需要两个约束 → 用 & 连接
     *
     * 适用场景：
     * - 需要对数字进行比较和运算
     * - Integer、Double 等都同时满足这两个约束
     *
     * 💡 重要提示：
     * - 不能写成 <T extends Comparable<T> & Number> 也可以！
     * - 因为都是接口，顺序不重要
     */
    private static <T extends Number & Comparable<T>> void printRange(List<T> list) {
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
//     * ✅ 答案6（综合题）：
//     * - T 的约束：<T extends Comparable<T>>
//     * - src 通配符：List<? extends T>
//     * - dest 通配符：List<? super T>
//     *
//     * 原因分析：
//     * 1. T 需要 Comparable<T>：
//     *    - 因为要排序，需要调用 compareTo
//     *
//     * 2. src 用 ? extends T：
//     *    - src 是生产者，只需要读取
//     *    - PECS原则：Producer Extends
//     *
//     * 3. dest 用 ? super T：
//     *    - dest 是消费者，需要写入
//     *    - PECS原则：Consumer Super
//     *
//     * 这道题综合了：
//     * - Day 2 的通配符知识（extends/super）
//     * - Day 3 的类型约束知识（Comparable）
//     *
//     * 实际效果：
//     * - 可以把 List<Integer> 复制到 List<Number>
//     * - 可以把 List<String> 复制到 List<Object>
//     * - 只要目标类型是源类型的父类即可
//     */
//    private static <T extends Comparable<T>> void copyAndSort(
//        List<? extends T> src,
//        List<? super T> dest
//    ) {
//        // 1. 复制数据
//        for (T item : src) {
//            dest.add(item);
//        }
//
//        // 2. 排序（这里因为dest是? super T，操作起来比较复杂）
//        // 实际项目中会用 Collections.sort()
//        for (int i = 0; i < dest.size() - 1; i++) {
//            for (int j = 0; j < dest.size() - 1 - i; j++) {
//                @SuppressWarnings("unchecked")
//                Comparable<? super T> a = (Comparable<? super T>) dest.get(j);
//                @SuppressWarnings("unchecked")
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
