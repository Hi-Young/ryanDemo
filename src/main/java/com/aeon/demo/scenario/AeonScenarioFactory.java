package com.aeon.demo.scenario;

import com.aeon.demo.domain.Coupon;
import com.aeon.demo.domain.CouponCategory;
import com.aeon.demo.domain.CouponConditionType;
import com.aeon.demo.domain.Promotion;
import com.aeon.demo.domain.PromotionLevel;
import com.aeon.demo.domain.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 场景工厂：提供 S1 这一套可复现的「促销+券」数据。
 *
 * @author codex
 */
public class AeonScenarioFactory {

    public static final String SCENARIO_S1 = "S1";

    public AeonScenario getScenario(String scenarioId) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            return buildS1();
        }
        String id = scenarioId.trim().toUpperCase(Locale.ROOT);
        if (SCENARIO_S1.equals(id)) {
            return buildS1();
        }
        // 未知场景：默认给 S1，避免接口直接报错影响体验
        return buildS1();
    }

    private AeonScenario buildS1() {
        // 这里用 2099 年保证测试/演示时不会过期
        LocalDateTime farFuture = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        // -----------------------------
        // 促销（先算）
        // -----------------------------
        // 单品：苹果 直降 5 元/件
        Promotion p1001 = Promotion.builder(1001L, "P1001-苹果直降5元/件", PromotionLevel.SINGLE, PromotionType.DIRECT_REDUCTION)
                .skuScope(new HashSet<>(Collections.singletonList("SKU-APPLE")))
                .reduceAmount(new BigDecimal("5.00"))
                .build();

        // 单品：牛奶 9 折（给组合取低价提供对比项）
        Promotion p1002 = Promotion.builder(1002L, "P1002-牛奶9折", PromotionLevel.SINGLE, PromotionType.DISCOUNT_RATE)
                .skuScope(new HashSet<>(Collections.singletonList("SKU-MILK")))
                .discountRate(new BigDecimal("0.90"))
                .build();

        // 组合：牛肉+牛奶 满150减30（笛卡尔积穷举会发现它更优）
        Promotion p2001 = Promotion.builder(2001L, "P2001-牛肉牛奶满150减30", PromotionLevel.GROUP, PromotionType.FULL_REDUCTION)
                .skuScope(new HashSet<>(Arrays.asList("SKU-BEEF", "SKU-MILK")))
                .threshold(new BigDecimal("150.00"))
                .reduceAmount(new BigDecimal("30.00"))
                .build();

        List<Promotion> promotions = Arrays.asList(p1001, p1002, p2001);

        // -----------------------------
        // 券（后算，基于促销后价格 promoPrice）
        // -----------------------------
        List<Coupon> coupons = new ArrayList<>();

        // 商品券：70（全场，可叠加）
        coupons.add(Coupon.builder("G205-1", 205, CouponCategory.GOODS)
                .parValue(new BigDecimal("70.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // 商品券：50（全场，可叠加，但与模板205互斥）
        coupons.add(Coupon.builder("G201-1", 201, CouponCategory.GOODS)
                .parValue(new BigDecimal("50.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // 商品券：40（仅牛肉SKU，范围更窄，排序上更优先）
        coupons.add(Coupon.builder("G202-1", 202, CouponCategory.GOODS)
                .parValue(new BigDecimal("40.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.SKU)
                .skuScope(new HashSet<>(Collections.singletonList("SKU-BEEF")))
                .useEndTime(farFuture)
                .build());

        // 商品券：35（苹果+牛奶SKU）
        coupons.add(Coupon.builder("G203-1", 203, CouponCategory.GOODS)
                .parValue(new BigDecimal("35.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.SKU)
                .skuScope(new HashSet<>(Arrays.asList("SKU-APPLE", "SKU-MILK")))
                .useEndTime(farFuture)
                .build());

        // 商品券：20（不可与其他券叠加，用于演示 otherAddition=0）
        coupons.add(Coupon.builder("G204-1", 204, CouponCategory.GOODS)
                .parValue(new BigDecimal("20.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(0)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // 运费券：10（与商品券模板205互斥，演示 freightChecked=false）
        coupons.add(Coupon.builder("S301-1", 301, CouponCategory.SHIPPING)
                .parValue(new BigDecimal("10.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // 运费券：6
        coupons.add(Coupon.builder("S302-1", 302, CouponCategory.SHIPPING)
                .parValue(new BigDecimal("6.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // 运费券：5
        coupons.add(Coupon.builder("S303-1", 303, CouponCategory.SHIPPING)
                .parValue(new BigDecimal("5.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // 运费券：15（大于运费时默认不勾选，演示“防溢出”）
        coupons.add(Coupon.builder("S304-1", 304, CouponCategory.SHIPPING)
                .parValue(new BigDecimal("15.00"))
                .bound(new BigDecimal("0.00"))
                .otherAddition(1)
                .conditionType(CouponConditionType.ALL)
                .useEndTime(farFuture)
                .build());

        // -----------------------------
        // 互斥组（同组互斥）
        // -----------------------------
        Map<Integer, List<Integer>> groups = new HashMap<>();
        groups.put(9001, Arrays.asList(205, 201)); // 商品券模板互斥
        groups.put(9002, Arrays.asList(301, 205)); // 运费券模板301 与 商品券模板205 互斥

        // 运费阶梯（演示“用券后运费变更”）
        BigDecimal freeShippingThreshold = new BigDecimal("199.00");
        BigDecimal baseFreight = new BigDecimal("12.00");

        return new AeonScenario(SCENARIO_S1,
                "S1：先促销后用券 + 运费二次计算 + 运费券重算（最小可跑通示例）",
                promotions,
                coupons,
                groups,
                freeShippingThreshold,
                baseFreight);
    }
}

