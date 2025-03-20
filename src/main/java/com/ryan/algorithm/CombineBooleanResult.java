package com.ryan.algorithm;

import java.util.Arrays;
import java.util.List;

public class CombineBooleanResult {
    public static void main(String[] args) {
        // 使用 Arrays.asList 创建列表
        List<Boolean> booleanResult = Arrays.asList(false, true, false);
        List<Boolean> logicSign = Arrays.asList(true, false);

        boolean result = combineBooleanResult(booleanResult, logicSign);
        System.out.println("结果: " + result);  // 输出结果应为 true
    }

    private static boolean combineBooleanResult(List<Boolean> booleanResult, List<Boolean> logicSign) {
        // 如果 booleanResult 为空，则直接返回 false
        if (booleanResult.isEmpty()) {
            return false;
        }

        // 初始化结果为 booleanResult 的第一个元素
        boolean result = booleanResult.get(0);

        // 从第二个元素开始遍历 booleanResult
        for (int i = 1; i < booleanResult.size(); i++) {
            boolean currentValue = booleanResult.get(i);
            // i - 1 以匹配 logicSign 与 booleanResult 的索引
            boolean currentSign = logicSign.get(i - 1);

            if (currentSign) {
                // 如果当前逻辑符号为 true（AND），则进行 AND 操作
                result = result && currentValue;
            } else {
                // 如果当前逻辑符号为 false（OR），则进行 OR 操作并递归处理剩余的列表
                List<Boolean> remainingResults = booleanResult.subList(i, booleanResult.size());
                List<Boolean> remainingLogicSigns = logicSign.subList(i, logicSign.size());
                result = result || combineBooleanResult(remainingResults, remainingLogicSigns);
                break;
            }
        }

        return result;
    }
}
