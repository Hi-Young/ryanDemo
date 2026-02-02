package com.aeon.demo.dto;

import com.aeon.demo.domain.CouponCategory;
import com.aeon.demo.domain.CouponConditionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义券入参（可选）。
 *
 * @author codex
 */
public class CouponRequest {

    private String couponNo;
    private int couponTemplateId;
    private CouponCategory category;
    private BigDecimal parValue;
    private BigDecimal bound;
    private int otherAddition = 1;
    private CouponConditionType conditionType = CouponConditionType.ALL;
    private List<String> skuScope = new ArrayList<>();
    private String useEndTime;
    private Integer sameTemplateUseLimit;

    public String getCouponNo() {
        return couponNo;
    }

    public void setCouponNo(String couponNo) {
        this.couponNo = couponNo;
    }

    public int getCouponTemplateId() {
        return couponTemplateId;
    }

    public void setCouponTemplateId(int couponTemplateId) {
        this.couponTemplateId = couponTemplateId;
    }

    public CouponCategory getCategory() {
        return category;
    }

    public void setCategory(CouponCategory category) {
        this.category = category;
    }

    public BigDecimal getParValue() {
        return parValue;
    }

    public void setParValue(BigDecimal parValue) {
        this.parValue = parValue;
    }

    public BigDecimal getBound() {
        return bound;
    }

    public void setBound(BigDecimal bound) {
        this.bound = bound;
    }

    public int getOtherAddition() {
        return otherAddition;
    }

    public void setOtherAddition(int otherAddition) {
        this.otherAddition = otherAddition;
    }

    public CouponConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(CouponConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public List<String> getSkuScope() {
        return skuScope;
    }

    public void setSkuScope(List<String> skuScope) {
        this.skuScope = skuScope;
    }

    public String getUseEndTime() {
        return useEndTime;
    }

    public void setUseEndTime(String useEndTime) {
        this.useEndTime = useEndTime;
    }

    public Integer getSameTemplateUseLimit() {
        return sameTemplateUseLimit;
    }

    public void setSameTemplateUseLimit(Integer sameTemplateUseLimit) {
        this.sameTemplateUseLimit = sameTemplateUseLimit;
    }
}

