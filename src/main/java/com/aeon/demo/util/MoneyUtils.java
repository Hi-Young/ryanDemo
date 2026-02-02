package com.aeon.demo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额工具（统一 scale=2）。
 *
 * @author codex
 */
public final class MoneyUtils {

    public static final int MONEY_SCALE = 2;

    private MoneyUtils() {
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return zero();
        }
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}

