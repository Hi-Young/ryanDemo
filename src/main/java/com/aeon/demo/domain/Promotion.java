package com.aeon.demo.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 促销定义（内置数据，用于模拟 AEON 促销中台返回的命中促销）。
 *
 * @author codex
 */
public class Promotion {

    private final long promoId;
    private final String name;
    private final PromotionLevel level;
    private final PromotionType type;

    /**
     * 适用SKU范围；为空代表全场可用（本 Demo 用于演示，不做复杂类目/品牌）。
     */
    private final Set<String> skuScope;

    /**
     * 满减/满折门槛（金额）。
     */
    private final BigDecimal threshold;

    /**
     * 满减优惠金额 / 直降金额（单品：每件直降；组合：总额直减）。
     */
    private final BigDecimal reduceAmount;

    /**
     * 折扣率（例如 0.9 = 9折）。
     */
    private final BigDecimal discountRate;

    private Promotion(Builder builder) {
        this.promoId = builder.promoId;
        this.name = builder.name;
        this.level = builder.level;
        this.type = builder.type;
        this.skuScope = builder.skuScope == null ? Collections.emptySet() : Collections.unmodifiableSet(builder.skuScope);
        this.threshold = builder.threshold;
        this.reduceAmount = builder.reduceAmount;
        this.discountRate = builder.discountRate;
    }

    public long getPromoId() {
        return promoId;
    }

    public String getName() {
        return name;
    }

    public PromotionLevel getLevel() {
        return level;
    }

    public PromotionType getType() {
        return type;
    }

    public Set<String> getSkuScope() {
        return skuScope;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public BigDecimal getReduceAmount() {
        return reduceAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public boolean matchesSku(String skuId) {
        return skuScope.isEmpty() || skuScope.contains(skuId);
    }

    public static Builder builder(long promoId, String name, PromotionLevel level, PromotionType type) {
        return new Builder(promoId, name, level, type);
    }

    public static class Builder {
        private final long promoId;
        private final String name;
        private final PromotionLevel level;
        private final PromotionType type;

        private Set<String> skuScope;
        private BigDecimal threshold;
        private BigDecimal reduceAmount;
        private BigDecimal discountRate;

        private Builder(long promoId, String name, PromotionLevel level, PromotionType type) {
            this.promoId = promoId;
            this.name = name;
            this.level = level;
            this.type = type;
        }

        public Builder skuScope(Set<String> skuScope) {
            if (skuScope == null) {
                this.skuScope = null;
            } else {
                this.skuScope = new HashSet<>(skuScope);
            }
            return this;
        }

        public Builder threshold(BigDecimal threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder reduceAmount(BigDecimal reduceAmount) {
            this.reduceAmount = reduceAmount;
            return this;
        }

        public Builder discountRate(BigDecimal discountRate) {
            this.discountRate = discountRate;
            return this;
        }

        public Promotion build() {
            return new Promotion(this);
        }
    }
}

