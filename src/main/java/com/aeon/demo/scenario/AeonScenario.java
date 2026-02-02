package com.aeon.demo.scenario;

import com.aeon.demo.domain.Coupon;
import com.aeon.demo.domain.Promotion;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 内置场景数据：用一套固定的促销/券/互斥组/运费规则，方便你用 Postman 一键跑通流程。
 *
 * @author codex
 */
public class AeonScenario {

    private final String scenarioId;
    private final String description;

    private final List<Promotion> promotions;
    private final List<Coupon> coupons;
    /**
     * 互斥组：groupId -> templateId 列表（同组互斥）。
     */
    private final Map<Integer, List<Integer>> mutuallyExclusiveGroups;

    /**
     * 运费阶梯：商品实付 >= freeShippingThreshold 则免运费，否则收 baseFreight。
     */
    private final BigDecimal freeShippingThreshold;
    private final BigDecimal baseFreight;

    public AeonScenario(String scenarioId,
                        String description,
                        List<Promotion> promotions,
                        List<Coupon> coupons,
                        Map<Integer, List<Integer>> mutuallyExclusiveGroups,
                        BigDecimal freeShippingThreshold,
                        BigDecimal baseFreight) {
        this.scenarioId = scenarioId;
        this.description = description;
        this.promotions = promotions == null ? Collections.emptyList() : Collections.unmodifiableList(promotions);
        this.coupons = coupons == null ? Collections.emptyList() : Collections.unmodifiableList(coupons);
        this.mutuallyExclusiveGroups = mutuallyExclusiveGroups == null ? Collections.emptyMap() : Collections.unmodifiableMap(mutuallyExclusiveGroups);
        this.freeShippingThreshold = freeShippingThreshold;
        this.baseFreight = baseFreight;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getDescription() {
        return description;
    }

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public List<Coupon> getCoupons() {
        return coupons;
    }

    public Map<Integer, List<Integer>> getMutuallyExclusiveGroups() {
        return mutuallyExclusiveGroups;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public BigDecimal getBaseFreight() {
        return baseFreight;
    }
}

