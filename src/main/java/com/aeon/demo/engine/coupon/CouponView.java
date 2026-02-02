package com.aeon.demo.engine.coupon;

import com.aeon.demo.domain.CouponCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 面向接口返回的券视图（简化版 AvailableCouponDTO）。
 *
 * @author codex
 */
public class CouponView {

    public static final int STATUS_USABLE = 1;
    public static final int STATUS_UN_USABLE = 0;

    private String couponNo;
    private int couponTemplateId;
    private CouponCategory category;

    private BigDecimal parValue;
    private BigDecimal bound;
    private int otherAddition;
    private int conditionType;

    /**
     * 商品券：适用商品金额；运费券：运费金额。
     */
    private BigDecimal applicableAmount;

    private boolean checked;
    private int usableStatus;
    private String usableStatusDesc;

    /**
     * 运费券：是否与已选商品券互斥（true=不互斥，可用；false=互斥）
     */
    private Boolean freightChecked;

    /**
     * 适用商品行（用于排序：适用范围越窄越优先）
     */
    private List<String> applicableCartItemIds = new ArrayList<>();

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

    public int getConditionType() {
        return conditionType;
    }

    public void setConditionType(int conditionType) {
        this.conditionType = conditionType;
    }

    public BigDecimal getApplicableAmount() {
        return applicableAmount;
    }

    public void setApplicableAmount(BigDecimal applicableAmount) {
        this.applicableAmount = applicableAmount;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getUsableStatus() {
        return usableStatus;
    }

    public void setUsableStatus(int usableStatus) {
        this.usableStatus = usableStatus;
    }

    public String getUsableStatusDesc() {
        return usableStatusDesc;
    }

    public void setUsableStatusDesc(String usableStatusDesc) {
        this.usableStatusDesc = usableStatusDesc;
    }

    public Boolean getFreightChecked() {
        return freightChecked;
    }

    public void setFreightChecked(Boolean freightChecked) {
        this.freightChecked = freightChecked;
    }

    public List<String> getApplicableCartItemIds() {
        return applicableCartItemIds;
    }

    public void setApplicableCartItemIds(List<String> applicableCartItemIds) {
        this.applicableCartItemIds = applicableCartItemIds;
    }
}

