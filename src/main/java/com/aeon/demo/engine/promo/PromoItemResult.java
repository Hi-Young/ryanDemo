package com.aeon.demo.engine.promo;

import java.math.BigDecimal;

/**
 * 单品促销结果（类似 AEON PromoCartItemResponse / ItemCurrentInfoResponse 的合并简化版）。
 *
 * @author codex
 */
public class PromoItemResult {

    private String cartItemId;
    private String skuId;
    private int quantity;

    private BigDecimal salePrice; // 原价(单价)
    private BigDecimal promoPrice; // 促销后单价

    private BigDecimal saleAmount; // 原价行总额
    private BigDecimal promoAmount; // 促销后行总额（用于传给券：promoPrice*qty 的“商品净额”）

    private long appliedPromoId;
    private String appliedPromoName;

    private BigDecimal promoDiscountAmount; // 此商品行促销优惠金额（行总价差额）

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(BigDecimal promoPrice) {
        this.promoPrice = promoPrice;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getPromoAmount() {
        return promoAmount;
    }

    public void setPromoAmount(BigDecimal promoAmount) {
        this.promoAmount = promoAmount;
    }

    public long getAppliedPromoId() {
        return appliedPromoId;
    }

    public void setAppliedPromoId(long appliedPromoId) {
        this.appliedPromoId = appliedPromoId;
    }

    public String getAppliedPromoName() {
        return appliedPromoName;
    }

    public void setAppliedPromoName(String appliedPromoName) {
        this.appliedPromoName = appliedPromoName;
    }

    public BigDecimal getPromoDiscountAmount() {
        return promoDiscountAmount;
    }

    public void setPromoDiscountAmount(BigDecimal promoDiscountAmount) {
        this.promoDiscountAmount = promoDiscountAmount;
    }
}
