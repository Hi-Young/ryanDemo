package com.aeon.demo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 按权重分摊金额（用于：组合促销/券把总优惠分摊到各商品行）。
 *
 * <p>特点：保证分摊和=total（最后一个元素吃掉舍入误差）。</p>
 *
 * @author codex
 */
public final class MoneyAllocator {

    private MoneyAllocator() {
    }

    public static <K> Map<K, BigDecimal> allocate(BigDecimal total, Map<K, BigDecimal> weights) {
        Map<K, BigDecimal> result = new LinkedHashMap<>();
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0 || weights == null || weights.isEmpty()) {
            return result;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal w : weights.values()) {
            if (w != null && w.compareTo(BigDecimal.ZERO) > 0) {
                sum = sum.add(w);
            }
        }
        if (sum.compareTo(BigDecimal.ZERO) <= 0) {
            return result;
        }

        BigDecimal remaining = total.setScale(MoneyUtils.MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal remainingWeight = sum;

        int i = 0;
        int n = weights.size();
        for (Map.Entry<K, BigDecimal> e : weights.entrySet()) {
            i++;
            BigDecimal w = e.getValue() == null ? BigDecimal.ZERO : e.getValue();
            BigDecimal alloc;
            if (i == n) {
                alloc = remaining;
            } else {
                if (w.compareTo(BigDecimal.ZERO) <= 0) {
                    alloc = MoneyUtils.zero();
                } else {
                    BigDecimal ratio = w.divide(remainingWeight, 10, RoundingMode.HALF_UP);
                    alloc = total.multiply(ratio).setScale(MoneyUtils.MONEY_SCALE, RoundingMode.HALF_UP);
                }
                remaining = remaining.subtract(alloc);
                remainingWeight = remainingWeight.subtract(w);
            }
            result.put(e.getKey(), alloc);
        }
        return result;
    }
}

