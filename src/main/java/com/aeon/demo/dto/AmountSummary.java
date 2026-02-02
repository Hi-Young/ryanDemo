package com.aeon.demo.dto;

import java.math.BigDecimal;

/**
 * 金额汇总（方便你对照流程画图/讲解）。
 *
 * @author codex
 */
public class AmountSummary {

    private BigDecimal originalGoodsAmount;
    private BigDecimal promoGoodsAmount;

    private BigDecimal goodsCouponDiscount;
    private BigDecimal goodsPayAmount;

    private BigDecimal freightBeforeCoupon;
    private BigDecimal freightAfterGoodsCoupon;

    private BigDecimal shippingCouponDiscount;
    private BigDecimal freightPayAmount;

    private BigDecimal finalPayAmount;

    public BigDecimal getOriginalGoodsAmount() {
        return originalGoodsAmount;
    }

    public void setOriginalGoodsAmount(BigDecimal originalGoodsAmount) {
        this.originalGoodsAmount = originalGoodsAmount;
    }

    public BigDecimal getPromoGoodsAmount() {
        return promoGoodsAmount;
    }

    public void setPromoGoodsAmount(BigDecimal promoGoodsAmount) {
        this.promoGoodsAmount = promoGoodsAmount;
    }

    public BigDecimal getGoodsCouponDiscount() {
        return goodsCouponDiscount;
    }

    public void setGoodsCouponDiscount(BigDecimal goodsCouponDiscount) {
        this.goodsCouponDiscount = goodsCouponDiscount;
    }

    public BigDecimal getGoodsPayAmount() {
        return goodsPayAmount;
    }

    public void setGoodsPayAmount(BigDecimal goodsPayAmount) {
        this.goodsPayAmount = goodsPayAmount;
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

    public BigDecimal getShippingCouponDiscount() {
        return shippingCouponDiscount;
    }

    public void setShippingCouponDiscount(BigDecimal shippingCouponDiscount) {
        this.shippingCouponDiscount = shippingCouponDiscount;
    }

    public BigDecimal getFreightPayAmount() {
        return freightPayAmount;
    }

    public void setFreightPayAmount(BigDecimal freightPayAmount) {
        this.freightPayAmount = freightPayAmount;
    }

    public BigDecimal getFinalPayAmount() {
        return finalPayAmount;
    }

    public void setFinalPayAmount(BigDecimal finalPayAmount) {
        this.finalPayAmount = finalPayAmount;
    }
}

