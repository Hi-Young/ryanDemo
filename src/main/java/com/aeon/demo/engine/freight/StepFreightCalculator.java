package com.aeon.demo.engine.freight;

import com.aeon.demo.util.MoneyUtils;

import java.math.BigDecimal;

/**
 * 阶梯运费：>= threshold 免运费，否则 baseFreight。
 *
 * <p>用于演示 AEON 文档中的“用券后金额变了 → 需要二次算运费 → 运费券也要重算”。</p>
 *
 * @author codex
 */
public class StepFreightCalculator implements FreightCalculator {

    private final BigDecimal freeShippingThreshold;
    private final BigDecimal baseFreight;

    public StepFreightCalculator(BigDecimal freeShippingThreshold, BigDecimal baseFreight) {
        this.freeShippingThreshold = MoneyUtils.scale(freeShippingThreshold);
        this.baseFreight = MoneyUtils.scale(baseFreight);
    }

    @Override
    public BigDecimal calc(BigDecimal goodsPayAmount) {
        BigDecimal pay = MoneyUtils.scale(goodsPayAmount);
        if (pay.compareTo(freeShippingThreshold) >= 0) {
            return MoneyUtils.zero();
        }
        return baseFreight;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public BigDecimal getBaseFreight() {
        return baseFreight;
    }
}

