package com.aeon.demo.engine.promo;

import java.math.BigDecimal;

/**
 * 促销调整明细（类似 AEON PromoCartAdjustResponse 的最小字段集）。
 *
 * @author codex
 */
public class PromoAdjustResult {

    private long promoId;
    private String promoName;
    private BigDecimal promoAmount; // 该促销贡献的总优惠金额

    public PromoAdjustResult() {
    }

    public PromoAdjustResult(long promoId, String promoName, BigDecimal promoAmount) {
        this.promoId = promoId;
        this.promoName = promoName;
        this.promoAmount = promoAmount;
    }

    public long getPromoId() {
        return promoId;
    }

    public void setPromoId(long promoId) {
        this.promoId = promoId;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public BigDecimal getPromoAmount() {
        return promoAmount;
    }

    public void setPromoAmount(BigDecimal promoAmount) {
        this.promoAmount = promoAmount;
    }
}

