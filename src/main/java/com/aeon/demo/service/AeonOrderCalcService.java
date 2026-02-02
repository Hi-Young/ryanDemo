package com.aeon.demo.service;

import com.aeon.demo.domain.CartItem;
import com.aeon.demo.domain.Coupon;
import com.aeon.demo.dto.*;
import com.aeon.demo.engine.coupon.CouponCalcEngine;
import com.aeon.demo.engine.coupon.CouponCalcResult;
import com.aeon.demo.engine.coupon.CouponView;
import com.aeon.demo.engine.coupon.MutuallyExclusivePolicy;
import com.aeon.demo.engine.freight.StepFreightCalculator;
import com.aeon.demo.engine.promo.PromoCalcEngine;
import com.aeon.demo.engine.promo.PromoCalcResult;
import com.aeon.demo.scenario.AeonScenario;
import com.aeon.demo.scenario.AeonScenarioFactory;
import com.aeon.demo.util.DateTimeUtils;
import com.aeon.demo.util.MoneyUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单确认编排服务：固定顺序「先促销，后用券；用券后运费二次计算，运费券重算」。
 *
 * <p>这里对应 AEON 文档里的 OrderConfirmBuilder 链：promoInfo() → couponInfo() → 运费二次计算。</p>
 *
 * @author codex
 */
@Service
@Profile("aeon-demo")
public class AeonOrderCalcService {

    private final AeonScenarioFactory scenarioFactory = new AeonScenarioFactory();
    private final PromoCalcEngine promoCalcEngine = new PromoCalcEngine();
    private final CouponCalcEngine couponCalcEngine = new CouponCalcEngine();

    public AeonOrderCalcResponse calc(AeonOrderCalcRequest request) {
        AeonOrderCalcRequest req = request == null ? new AeonOrderCalcRequest() : request;

        String scenarioId = normalizeScenario(req.getScenario());
        AeonScenario scenario = scenarioFactory.getScenario(scenarioId);

        // 互斥组
        MutuallyExclusivePolicy exclusivePolicy = new MutuallyExclusivePolicy(scenario.getMutuallyExclusiveGroups());

        // 促销引擎（固定使用场景内置促销）
        List<CartItem> cartItems = toCartItems(req.getCartItems());
        PromoCalcResult promo = promoCalcEngine.calc(cartItems, scenario.getPromotions());

        // 运费计算器（场景内置）
        StepFreightCalculator freightCalculator = new StepFreightCalculator(scenario.getFreeShippingThreshold(), scenario.getBaseFreight());

        LocalDateTime now = LocalDateTime.now();

        // 用券前运费：可由外部传入（模拟“先算一次运费”），否则按规则算
        BigDecimal freightBefore = req.getLogisticFee() == null
                ? freightCalculator.calc(promo.getPromoGoodsAmount())
                : MoneyUtils.scale(req.getLogisticFee());

        // 券列表：可自定义；为空则用场景内置券
        List<Coupon> coupons = req.getCoupons() == null || req.getCoupons().isEmpty()
                ? scenario.getCoupons()
                : toCoupons(req.getCoupons());

        // -----------------------------
        // 8. couponInfo()：先算商品券
        // -----------------------------
        CouponCalcResult goodsCoupons = couponCalcEngine.calcGoodsCoupons(promo.getItems(), coupons, exclusivePolicy, now);
        BigDecimal goodsCouponDiscount = MoneyUtils.scale(goodsCoupons.getDiscountAmount());

        BigDecimal goodsPay = MoneyUtils.max(MoneyUtils.zero(), MoneyUtils.scale(promo.getPromoGoodsAmount()).subtract(goodsCouponDiscount));

        Set<Integer> selectedGoodsTemplateIds = goodsCoupons.getAvailableCouponList().stream()
                .filter(CouponView::isChecked)
                .map(CouponView::getCouponTemplateId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 运费券（用券前，基于 freightBefore）
        CouponCalcResult shippingBeforeRecalc = couponCalcEngine.calcShippingCoupons(freightBefore, coupons, selectedGoodsTemplateIds, exclusivePolicy, now);

        // -----------------------------
        // 运费二次计算：用券后金额变更，运费阶梯可能变化
        // -----------------------------
        BigDecimal freightAfterGoodsCoupon = freightCalculator.calc(goodsPay);

        // 运费券重算（基于 freightAfterGoodsCoupon）
        CouponCalcResult shippingAfterRecalc = couponCalcEngine.calcShippingCoupons(freightAfterGoodsCoupon, coupons, selectedGoodsTemplateIds, exclusivePolicy, now);
        BigDecimal shippingDiscount = MoneyUtils.scale(shippingAfterRecalc.getDiscountAmount());

        BigDecimal freightPay = MoneyUtils.max(MoneyUtils.zero(), MoneyUtils.scale(freightAfterGoodsCoupon).subtract(shippingDiscount));
        BigDecimal finalPay = MoneyUtils.scale(goodsPay.add(freightPay));

        // -----------------------------
        // 组装出参（把关键中间态都返回）
        // -----------------------------
        AeonOrderCalcResponse resp = new AeonOrderCalcResponse();
        resp.setScenario(scenario.getScenarioId());
        resp.setScenarioDesc(scenario.getDescription());
        resp.setPromo(promo);
        resp.setGoodsCoupons(goodsCoupons);
        resp.setShippingCouponsBeforeFreightRecalc(shippingBeforeRecalc);
        resp.setShippingCouponsAfterFreightRecalc(shippingAfterRecalc);

        FreightInfo freight = new FreightInfo();
        freight.setFreeShippingThreshold(freightCalculator.getFreeShippingThreshold());
        freight.setBaseFreight(freightCalculator.getBaseFreight());
        freight.setFreightBeforeCoupon(MoneyUtils.scale(freightBefore));
        freight.setFreightAfterGoodsCoupon(MoneyUtils.scale(freightAfterGoodsCoupon));
        resp.setFreight(freight);

        AmountSummary summary = new AmountSummary();
        summary.setOriginalGoodsAmount(promo.getOriginalGoodsAmount());
        summary.setPromoGoodsAmount(promo.getPromoGoodsAmount());
        summary.setGoodsCouponDiscount(goodsCouponDiscount);
        summary.setGoodsPayAmount(goodsPay);
        summary.setFreightBeforeCoupon(MoneyUtils.scale(freightBefore));
        summary.setFreightAfterGoodsCoupon(MoneyUtils.scale(freightAfterGoodsCoupon));
        summary.setShippingCouponDiscount(shippingDiscount);
        summary.setFreightPayAmount(freightPay);
        summary.setFinalPayAmount(finalPay);
        resp.setAmountSummary(summary);

        resp.setTrace(buildTrace(promo, goodsCoupons, shippingBeforeRecalc, freightBefore, goodsPay, freightAfterGoodsCoupon, shippingAfterRecalc, finalPay));
        return resp;
    }

    private static String normalizeScenario(String scenario) {
        if (scenario == null || scenario.trim().isEmpty()) {
            return AeonScenarioFactory.SCENARIO_S1;
        }
        return scenario.trim().toUpperCase(Locale.ROOT);
    }

    private static List<CartItem> toCartItems(List<CartItemRequest> reqItems) {
        if (reqItems == null || reqItems.isEmpty()) {
            // Demo 要求最小可跑通：不传就返回空（由上层决定是否报错/提供 sample）
            return Collections.emptyList();
        }
        List<CartItem> list = new ArrayList<>();
        int idx = 1;
        for (CartItemRequest r : reqItems) {
            if (r == null) {
                continue;
            }
            if (r.getQuantity() <= 0) {
                continue;
            }
            String cartItemId = (r.getCartItemId() == null || r.getCartItemId().trim().isEmpty())
                    ? "C" + (idx++)
                    : r.getCartItemId().trim();
            String skuId = r.getSkuId() == null ? "" : r.getSkuId().trim();
            int qty = r.getQuantity();
            BigDecimal salePrice = r.getSalePrice() == null ? MoneyUtils.zero() : MoneyUtils.scale(r.getSalePrice());
            list.add(new CartItem(cartItemId, skuId, qty, salePrice));
        }
        return list;
    }

    private static List<Coupon> toCoupons(List<CouponRequest> reqCoupons) {
        if (reqCoupons == null) {
            return Collections.emptyList();
        }
        List<Coupon> list = new ArrayList<>();
        for (CouponRequest r : reqCoupons) {
            if (r == null) {
                continue;
            }
            if (r.getCategory() == null) {
                continue;
            }
            String couponNo = (r.getCouponNo() == null || r.getCouponNo().trim().isEmpty())
                    ? ("U-" + r.getCouponTemplateId() + "-" + (list.size() + 1))
                    : r.getCouponNo().trim();
            Coupon.Builder b = Coupon.builder(couponNo, r.getCouponTemplateId(), r.getCategory());
            b.parValue(r.getParValue());
            b.bound(r.getBound());
            b.otherAddition(r.getOtherAddition());
            b.conditionType(r.getConditionType());
            if (r.getSkuScope() != null && !r.getSkuScope().isEmpty()) {
                b.skuScope(new HashSet<>(r.getSkuScope()));
            }
            b.useEndTime(DateTimeUtils.parseOrNull(r.getUseEndTime()));
            if (r.getSameTemplateUseLimit() != null) {
                b.sameTemplateUseLimit(r.getSameTemplateUseLimit());
            }
            list.add(b.build());
        }
        return list;
    }

    private static List<String> buildTrace(PromoCalcResult promo,
                                          CouponCalcResult goodsCoupons,
                                          CouponCalcResult shippingBefore,
                                          BigDecimal freightBefore,
                                          BigDecimal goodsPay,
                                          BigDecimal freightAfter,
                                          CouponCalcResult shippingAfter,
                                          BigDecimal finalPay) {
        List<String> t = new ArrayList<>();
        t.add("流程要点：先促销，后用券；用券后金额变化会触发运费二次计算与运费券重算。");
        t.add("1) 原价商品总额 = " + promo.getOriginalGoodsAmount());
        t.add("5) 促销后商品总额(promoPrice汇总) = " + promo.getPromoGoodsAmount()
                + "，穷举方案数 = " + promo.getEvaluatedPlanCount());
        t.add("8) 商品券推荐优惠 = " + MoneyUtils.scale(goodsCoupons.getDiscountAmount())
                + "，商品实付 = " + goodsPay);
        t.add("运费(用券前) = " + MoneyUtils.scale(freightBefore)
                + "；运费(用券后二次计算) = " + MoneyUtils.scale(freightAfter));
        t.add("运费券(用券前)推荐优惠 = " + MoneyUtils.scale(shippingBefore.getDiscountAmount())
                + "；运费券(重算后)推荐优惠 = " + MoneyUtils.scale(shippingAfter.getDiscountAmount()));
        t.add("最终应付 = " + finalPay);
        return t;
    }
}
