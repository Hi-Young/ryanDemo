package com.aeon.demo.engine.freight;

import java.math.BigDecimal;

/**
 * 运费计算器（最小化）。
 *
 * @author codex
 */
public interface FreightCalculator {

    /**
     * @param goodsPayAmount 商品实付金额（通常：促销后总额 - 商品券优惠）
     * @return 运费
     */
    BigDecimal calc(BigDecimal goodsPayAmount);
}

