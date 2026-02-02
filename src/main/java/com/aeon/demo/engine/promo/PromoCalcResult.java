package com.aeon.demo.engine.promo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 促销计算结果（最小化）。
 *
 * @author codex
 */
public class PromoCalcResult {

    private BigDecimal originalGoodsAmount;
    private BigDecimal promoGoodsAmount;

    private List<PromoItemResult> items = new ArrayList<>();
    private List<PromoAdjustResult> adjusts = new ArrayList<>();

    /**
     * 为了讲清楚“组合取低价”的穷举过程，这里返回本次评估的方案数（可选）。
     */
    private long evaluatedPlanCount;

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

    public List<PromoItemResult> getItems() {
        return items;
    }

    public void setItems(List<PromoItemResult> items) {
        this.items = items;
    }

    public List<PromoAdjustResult> getAdjusts() {
        return adjusts;
    }

    public void setAdjusts(List<PromoAdjustResult> adjusts) {
        this.adjusts = adjusts;
    }

    public long getEvaluatedPlanCount() {
        return evaluatedPlanCount;
    }

    public void setEvaluatedPlanCount(long evaluatedPlanCount) {
        this.evaluatedPlanCount = evaluatedPlanCount;
    }
}

