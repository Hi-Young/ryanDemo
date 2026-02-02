package com.aeon.demo.dto;

import java.math.BigDecimal;

/**
 * 运费信息（用于展示“二次计算”前后变化）。
 *
 * @author codex
 */
public class FreightInfo {

    private BigDecimal freeShippingThreshold;
    private BigDecimal baseFreight;

    private BigDecimal freightBeforeCoupon;
    private BigDecimal freightAfterGoodsCoupon;

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getBaseFreight() {
        return baseFreight;
    }

    public void setBaseFreight(BigDecimal baseFreight) {
        this.baseFreight = baseFreight;
    }

    public BigDecimal getFreightBeforeCoupon() {
        return freightBeforeCoupon;
    }

    public void setFreightBeforeCoupon(BigDecimal freightBeforeCoupon) {
        this.freightBeforeCoupon = freightBeforeCoupon;
    }

    public BigDecimal getFreightAfterGoodsCoupon() {
        return freightAfterGoodsCoupon;
    }

    public void setFreightAfterGoodsCoupon(BigDecimal freightAfterGoodsCoupon) {
        this.freightAfterGoodsCoupon = freightAfterGoodsCoupon;
    }
}

