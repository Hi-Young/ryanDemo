package com.aeon.demo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 下单确认(演示)入参：用于跑通「先促销后用券 + 运费二次计算」流程。
 *
 * @author codex
 */
public class AeonOrderCalcRequest {

    /**
     * 场景ID（默认 S1）。
     */
    private String scenario;

    private String memberId;
    private String storeCode;
    private Integer channel;
    private String platform;

    /**
     * 用券前运费（可选）：为空则按场景运费规则自动计算。
     */
    private BigDecimal logisticFee;

    private List<CartItemRequest> cartItems = new ArrayList<>();

    /**
     * 可选：自定义券列表；为空则使用场景内置券。
     */
    private List<CouponRequest> coupons = new ArrayList<>();

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public BigDecimal getLogisticFee() {
        return logisticFee;
    }

    public void setLogisticFee(BigDecimal logisticFee) {
        this.logisticFee = logisticFee;
    }

    public List<CartItemRequest> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemRequest> cartItems) {
        this.cartItems = cartItems;
    }

    public List<CouponRequest> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<CouponRequest> coupons) {
        this.coupons = coupons;
    }
}

