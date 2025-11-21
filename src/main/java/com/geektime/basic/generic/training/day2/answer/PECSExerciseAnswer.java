package com.geektime.basic.generic.training.day2.answer;

import java.util.List;

/**
 * 参考答案：PECS原则练习
 *
 * ⚠️ 先自己实现，实在卡住了再看这个答案！
 */
public class PECSExerciseAnswer {

    /**
     * 答案1：找最大值（读取 → extends）
     */
    private static <T extends Comparable<T>> T findMax(List<? extends T> list) {
        if (list == null || list.isEmpty()) {
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

    /**
     * 答案2：批量添加（写入 → super，读取 → extends）
     */
    private static <T> void addAll(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {
            dest.add(item);
        }
    }

    /**
     * 答案3：求和（读取 → extends）
     */
    private static double sum(List<? extends Number> numbers) {
        double sum = 0;
        for (Number num : numbers) {
            sum += num.doubleValue();
        }
        return sum;
    }
}
