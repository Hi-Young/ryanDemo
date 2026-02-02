package com.aeon.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 优惠券（内置数据/或由接口入参传入）。
 *
 * @author codex
 */
public class Coupon {

    private final String couponNo;
    private final int couponTemplateId;
    private final CouponCategory category;
    private final BigDecimal parValue; // 面值
    private final BigDecimal bound; // 使用门槛（最小化：对商品券=适用商品金额门槛；对运费券=运费门槛）
    private final int otherAddition; // 0=不可与其他券叠加 1=可叠加
    private final CouponConditionType conditionType;
    private final Set<String> skuScope;
    private final LocalDateTime useEndTime;
    private final int sameTemplateUseLimit; // 同模板叠加上限（最小化：默认1）

    private Coupon(Builder builder) {
        this.couponNo = builder.couponNo;
        this.couponTemplateId = builder.couponTemplateId;
        this.category = builder.category;
        this.parValue = builder.parValue;
        this.bound = builder.bound;
        this.otherAddition = builder.otherAddition;
        this.conditionType = builder.conditionType;
        this.skuScope = builder.skuScope == null ? Collections.emptySet() : Collections.unmodifiableSet(builder.skuScope);
        this.useEndTime = builder.useEndTime;
        this.sameTemplateUseLimit = builder.sameTemplateUseLimit;
    }

    public String getCouponNo() {
        return couponNo;
    }

    public int getCouponTemplateId() {
        return couponTemplateId;
    }

    public CouponCategory getCategory() {
        return category;
    }

    public BigDecimal getParValue() {
        return parValue;
    }

    public BigDecimal getBound() {
        return bound;
    }

    public int getOtherAddition() {
        return otherAddition;
    }

    public CouponConditionType getConditionType() {
        return conditionType;
    }

    public Set<String> getSkuScope() {
        return skuScope;
    }

    public LocalDateTime getUseEndTime() {
        return useEndTime;
    }

    public int getSameTemplateUseLimit() {
        return sameTemplateUseLimit;
    }

    public boolean matchesSku(String skuId) {
        return skuScope.isEmpty() || skuScope.contains(skuId);
    }

    public static Builder builder(String couponNo, int couponTemplateId, CouponCategory category) {
        return new Builder(couponNo, couponTemplateId, category);
    }

    public static class Builder {
        private final String couponNo;
        private final int couponTemplateId;
        private final CouponCategory category;

        private BigDecimal parValue;
        private BigDecimal bound;
        private int otherAddition = 1;
        private CouponConditionType conditionType = CouponConditionType.ALL;
        private Set<String> skuScope;
        private LocalDateTime useEndTime;
        private int sameTemplateUseLimit = 1;

        private Builder(String couponNo, int couponTemplateId, CouponCategory category) {
            this.couponNo = couponNo;
            this.couponTemplateId = couponTemplateId;
            this.category = category;
        }

        public Builder parValue(BigDecimal parValue) {
            this.parValue = parValue;
            return this;
        }

        public Builder bound(BigDecimal bound) {
            this.bound = bound;
            return this;
        }

        public Builder otherAddition(int otherAddition) {
            this.otherAddition = otherAddition;
            return this;
        }

        public Builder conditionType(CouponConditionType conditionType) {
            this.conditionType = conditionType;
            return this;
        }

        public Builder skuScope(Set<String> skuScope) {
            if (skuScope == null) {
                this.skuScope = null;
            } else {
                this.skuScope = new HashSet<>(skuScope);
            }
            return this;
        }

        public Builder useEndTime(LocalDateTime useEndTime) {
            this.useEndTime = useEndTime;
            return this;
        }

        public Builder sameTemplateUseLimit(int sameTemplateUseLimit) {
            this.sameTemplateUseLimit = sameTemplateUseLimit;
            return this;
        }

        public Coupon build() {
            return new Coupon(this);
        }
    }
}

