package com.aeon.demo.dto;

import com.aeon.demo.engine.coupon.CouponCalcResult;
import com.aeon.demo.engine.promo.PromoCalcResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 下单确认(演示)出参：把关键中间态都返回，方便你对照 AEON 文档理解流程。
 *
 * @author codex
 */
public class AeonOrderCalcResponse {

    private String scenario;
    private String scenarioDesc;

    private PromoCalcResult promo;

    private CouponCalcResult goodsCoupons;
    private CouponCalcResult shippingCouponsBeforeFreightRecalc;
    private CouponCalcResult shippingCouponsAfterFreightRecalc;

    private FreightInfo freight;
    private AmountSummary amountSummary;

    private List<String> trace = new ArrayList<>();

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getScenarioDesc() {
        return scenarioDesc;
    }

    public void setScenarioDesc(String scenarioDesc) {
        this.scenarioDesc = scenarioDesc;
    }

    public PromoCalcResult getPromo() {
        return promo;
    }

    public void setPromo(PromoCalcResult promo) {
        this.promo = promo;
    }

    public CouponCalcResult getGoodsCoupons() {
        return goodsCoupons;
    }

    public void setGoodsCoupons(CouponCalcResult goodsCoupons) {
        this.goodsCoupons = goodsCoupons;
    }

    public CouponCalcResult getShippingCouponsBeforeFreightRecalc() {
        return shippingCouponsBeforeFreightRecalc;
    }

    public void setShippingCouponsBeforeFreightRecalc(CouponCalcResult shippingCouponsBeforeFreightRecalc) {
        this.shippingCouponsBeforeFreightRecalc = shippingCouponsBeforeFreightRecalc;
    }

    public CouponCalcResult getShippingCouponsAfterFreightRecalc() {
        return shippingCouponsAfterFreightRecalc;
    }

    public void setShippingCouponsAfterFreightRecalc(CouponCalcResult shippingCouponsAfterFreightRecalc) {
        this.shippingCouponsAfterFreightRecalc = shippingCouponsAfterFreightRecalc;
    }

    public FreightInfo getFreight() {
        return freight;
    }

    public void setFreight(FreightInfo freight) {
        this.freight = freight;
    }

    public AmountSummary getAmountSummary() {
        return amountSummary;
    }

    public void setAmountSummary(AmountSummary amountSummary) {
        this.amountSummary = amountSummary;
    }

    public List<String> getTrace() {
        return trace;
    }

    public void setTrace(List<String> trace) {
        this.trace = trace;
    }
}

