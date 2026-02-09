package com.geektime.basic.generic.training.day2.after;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PECS原则练习
 *
 * Producer Extends, Consumer Super
 *
 * 🎯 你的任务：判断每个方法应该用 extends 还是 super
 */
public class PECSExercise {

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("   PECS原则练习");
        System.out.println("========================================\n");

        testFindMax();
        testAddAll();
        testSum();

        System.out.println("\n========================================");
        System.out.println("💡 PECS原则记忆：");
        System.out.println("----------------------------------------");
        System.out.println("Producer Extends - 从集合读数据用 extends");
        System.out.println("Consumer Super   - 往集合写数据用 super");
        System.out.println("========================================");
    }

    /**
     * 练习1：找出最大值（从集合读取数据）
     */
    private static void testFindMax() throws Exception {
        System.out.println("【练习1】找出最大值");
        System.out.println("----------------------------------------");

        List<Integer> integers = Arrays.asList(1, 5, 3, 9, 2);
        List<Double> doubles = Arrays.asList(1.5, 3.2, 2.1);

        Integer maxInt = findMax(integers);
        Double maxDouble = findMax(doubles);

        System.out.println("✓ 整数列表的最大值: " + maxInt);
        System.out.println("✓ 浮点列表的最大值: " + maxDouble);
        System.out.println();
    }

    /**
     * 🎯 TODO: 完善方法签名
     *
     * 分析：
     * - 这个方法需要从列表中**读取**数据
     * - 列表是生产者(Producer)
     * - 应该用 ? extends T
     *
     * 提示：<T extends Comparable<T>> T findMax(List<? extends T> list)
     */
    private static <T extends Comparable<T>> T findMax(List<? extends T> list) throws Exception {
        // TODO: 实现找最大值逻辑
        // 1. 检查列表是否为空
        // 2. 遍历列表，用 compareTo 比较
        // 3. 返回最大值
        if (CollectionUtils.isEmpty(list)) {
            throw new Exception("列表为空");
        }
//        throw new UnsupportedOperationException("请实现这个方法");
        T maxValue = list.get(0);
        if (list.size() == 1) {
            return maxValue;
        }
        for (int i = 1; i <list.size(); i++) {
            if (maxValue.compareTo(list.get(i))<0) {
                maxValue = list.get(i);
            }
        }
        return maxValue;
    }

    /**
     * 练习2：批量添加（往集合写入数据）
     */
    private static void testAddAll() {
        System.out.println("【练习2】批量添加");
        System.out.println("----------------------------------------");

        List<Integer> src = Arrays.asList(1, 2, 3);
        List<Number> dest = new ArrayList<>();

        addAll(dest, src);

        System.out.println("✓ 已添加到目标列表: " + dest);
        System.out.println();
    }

    /**
     * 🎯 TODO: 完善方法签名
     *
     * 分析：
     * - 这个方法需要往 dest 中**写入**数据
     * - dest 是消费者(Consumer)
     * - 应该用 ? super T
     * - src 是生产者，用 ? extends T
     *
     * 提示：<T> void addAll(List<? super T> dest, List<? extends T> src)
     */
    private static <T> void addAll(List<? super T> dest, List<? extends T> src) {
        dest.addAll(src);
        // TODO: 实现批量添加逻辑
        // 遍历 src，把每个元素添加到 dest
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 练习3：计算总和（从集合读取数据）
     */
    private static void testSum() {
        System.out.println("【练习3】计算总和");
        System.out.println("----------------------------------------");

        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5);

        double sumInt = sum(integers);
        double sumDouble = sum(doubles);

        System.out.println("✓ 整数列表总和: " + sumInt);
        System.out.println("✓ 浮点列表总和: " + sumDouble);
        System.out.println();
    }

    /**
     * 🎯 TODO: 完善方法签名
     *
     * 分析：
     * - 这个方法需要从列表中**读取**数据
     * - 列表是生产者
     * - 应该用 ? extends Number（因为要支持Integer、Double等）
     *
     * 提示：double sum(List<? extends Number> numbers)
     */
    private static double sum(List<? extends Number> numbers) {
        double sum = 0.0;
        for (Number number : numbers) {
            sum += number.doubleValue();
        }
        return sum;
        // TODO: 实现求和逻辑
        // 遍历 numbers，调用 doubleValue() 累加
//        throw new UnsupportedOperationException("请实现这个方法");
    }
}
