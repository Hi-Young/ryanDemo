package com.aeon.demo.engine.coupon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 券计算结果（最小化，结构对齐 AEON CalcCouponListResponse）。
 *
 * @author codex
 */
public class CouponCalcResult {

    private BigDecimal discountAmount;
    private List<CouponView> availableCouponList = new ArrayList<>();
    private List<CouponView> notAvailableCouponList = new ArrayList<>();

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<CouponView> getAvailableCouponList() {
        return availableCouponList;
    }

    public void setAvailableCouponList(List<CouponView> availableCouponList) {
        this.availableCouponList = availableCouponList;
    }

    public List<CouponView> getNotAvailableCouponList() {
        return notAvailableCouponList;
    }

    public void setNotAvailableCouponList(List<CouponView> notAvailableCouponList) {
        this.notAvailableCouponList = notAvailableCouponList;
    }
}

