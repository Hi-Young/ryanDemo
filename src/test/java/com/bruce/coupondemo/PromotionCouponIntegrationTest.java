package com.bruce.coupondemo;

import com.bruce.coupondemo.model.Coupon;
import com.bruce.coupondemo.model.CouponType;
import com.bruce.coupondemo.model.OrchestrationResult;
import com.bruce.coupondemo.service.OrderCalcService;
import com.bruce.promotiondemo.model.CartItem;
import com.bruce.promotiondemo.model.Rule;
import com.bruce.promotiondemo.model.RuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 促销+券联动 端到端集成测试
 */
class PromotionCouponIntegrationTest {

    private OrderCalcService orderCalcService;

    @BeforeEach
    void setUp() {
        orderCalcService = new OrderCalcService();
    }

    @Test
    @DisplayName("场景1: 纯促销(无券) - 200元满200减30 → 总付170")
    void testPurePromotion() {
        List<CartItem> items = Collections.singletonList(
                CartItem.builder().skuCode("SKU001").price(new BigDecimal("200")).quantity(1).build()
        );
        List<Rule> rules = Collections.singletonList(
                Rule.builder().id("R1").name("满200减30")
                        .type(RuleType.THRESHOLD_AMOUNT_OFF)
                        .threshold(new BigDecimal("200"))
                        .discount(new BigDecimal("30"))
                        .priority(1).build()
        );

        OrchestrationResult result = orderCalcService.calculate(items, rules, null, BigDecimal.ZERO);

        assertEquals(0, new BigDecimal("200").compareTo(result.getOriginalPrice()));
        assertEquals(0, new BigDecimal("30").compareTo(result.getPromotionDiscount()));
        assertEquals(0, new BigDecimal("170").compareTo(result.getPriceAfterPromotion()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getProductCouponDiscount()));
        assertEquals(0, new BigDecimal("170").compareTo(result.getTotalPayPrice()));
    }

    @Test
    @DisplayName("场景2: 纯用券(无促销) - 200元 + 50元券 + 10元运费券, 运费15 → 总付155")
    void testPureCoupon() {
        List<CartItem> items = Collections.singletonList(
                CartItem.builder().skuCode("SKU001").price(new BigDecimal("200")).quantity(1).build()
        );
        List<Coupon> coupons = Arrays.asList(
                Coupon.builder().couponId("C1").name("50元商品券")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("50"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build(),
                Coupon.builder().couponId("C2").name("10元运费券")
                        .couponType(CouponType.SHIPPING)
                        .faceValue(new BigDecimal("10"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build()
        );

        OrchestrationResult result = orderCalcService.calculate(
                items, null, coupons, new BigDecimal("15"));

        assertEquals(0, new BigDecimal("200").compareTo(result.getOriginalPrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getPromotionDiscount()));
        assertEquals(0, new BigDecimal("50").compareTo(result.getProductCouponDiscount()));
        assertEquals(0, new BigDecimal("10").compareTo(result.getShippingCouponDiscount()));
        // 商品应付 200-50=150, 运费应付 15-10=5, 总计 155
        assertEquals(0, new BigDecimal("155").compareTo(result.getTotalPayPrice()));
    }

    @Test
    @DisplayName("场景3: 促销+券联动 - 250元满200减30→220, 50元券(满150)可用, 运费15-10=5 → 总付175")
    void testPromotionAndCoupon() {
        List<CartItem> items = Arrays.asList(
                CartItem.builder().skuCode("SKU001").price(new BigDecimal("100")).quantity(2).build(),
                CartItem.builder().skuCode("SKU002").price(new BigDecimal("50")).quantity(1).build()
        );
        List<Rule> rules = Collections.singletonList(
                Rule.builder().id("R1").name("满200减30")
                        .type(RuleType.THRESHOLD_AMOUNT_OFF)
                        .threshold(new BigDecimal("200"))
                        .discount(new BigDecimal("30"))
                        .priority(1).build()
        );
        List<Coupon> coupons = Arrays.asList(
                Coupon.builder().couponId("C1").name("50元商品券")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("50"))
                        .threshold(new BigDecimal("150"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build(),
                Coupon.builder().couponId("C2").name("10元运费券")
                        .couponType(CouponType.SHIPPING)
                        .faceValue(new BigDecimal("10"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build()
        );

        OrchestrationResult result = orderCalcService.calculate(
                items, rules, coupons, new BigDecimal("15"));

        assertEquals(0, new BigDecimal("250").compareTo(result.getOriginalPrice()));
        assertEquals(0, new BigDecimal("30").compareTo(result.getPromotionDiscount()));
        assertEquals(0, new BigDecimal("220").compareTo(result.getPriceAfterPromotion()));
        assertEquals(0, new BigDecimal("50").compareTo(result.getProductCouponDiscount()));
        assertEquals(0, new BigDecimal("10").compareTo(result.getShippingCouponDiscount()));
        // 商品应付 220-50=170, 运费应付 15-10=5, 总计 175
        assertEquals(0, new BigDecimal("175").compareTo(result.getTotalPayPrice()));
    }

    @Test
    @DisplayName("场景4: 促销后券门槛不满足 - 200元8折→160, 50元券(满180)不可用 → 总付160")
    void testCouponThresholdNotMetAfterPromotion() {
        List<CartItem> items = Collections.singletonList(
                CartItem.builder().skuCode("SKU001").price(new BigDecimal("200")).quantity(1).build()
        );
        List<Rule> rules = Collections.singletonList(
                Rule.builder().id("R1").name("8折")
                        .type(RuleType.DISCOUNT)
                        .discount(new BigDecimal("0.8"))
                        .priority(1).build()
        );
        List<Coupon> coupons = Collections.singletonList(
                Coupon.builder().couponId("C1").name("50元商品券")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("50"))
                        .threshold(new BigDecimal("180"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build()
        );

        OrchestrationResult result = orderCalcService.calculate(
                items, rules, coupons, BigDecimal.ZERO);

        assertEquals(0, new BigDecimal("200").compareTo(result.getOriginalPrice()));
        // 促销后 200*0.8=160
        assertEquals(0, new BigDecimal("160").compareTo(result.getPriceAfterPromotion()));
        // 券门槛180，促销后160不满足 → 券不可用
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getProductCouponDiscount()));
        // 总付160
        assertEquals(0, new BigDecimal("160").compareTo(result.getTotalPayPrice()));
    }

    @Test
    @DisplayName("场景5: 券互斥 - 300元, 50元券(G1)+30元券(G1)+20元券(无组) → A选中B跳过C选中 → 总付230")
    void testCouponExclusiveGroup() {
        List<CartItem> items = Collections.singletonList(
                CartItem.builder().skuCode("SKU001").price(new BigDecimal("300")).quantity(1).build()
        );
        List<Coupon> coupons = Arrays.asList(
                Coupon.builder().couponId("C1").name("50元券A")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("50"))
                        .exclusiveGroupId("G1")
                        .expireDate(LocalDate.of(2026, 12, 31)).build(),
                Coupon.builder().couponId("C2").name("30元券B")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("30"))
                        .exclusiveGroupId("G1")
                        .expireDate(LocalDate.of(2026, 12, 31)).build(),
                Coupon.builder().couponId("C3").name("20元券C")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("20"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build()
        );

        OrchestrationResult result = orderCalcService.calculate(
                items, null, coupons, BigDecimal.ZERO);

        // A(50, G1) 选中, B(30, G1) 同组跳过, C(20, 无组) 选中 → 优惠70
        assertEquals(0, new BigDecimal("70").compareTo(result.getProductCouponDiscount()));
        assertEquals(0, new BigDecimal("230").compareTo(result.getTotalPayPrice()));

        // 验证选中状态
        List<Coupon> available = result.getCouponResult().getAvailableCoupons();
        Coupon couponA = available.stream().filter(c -> "C1".equals(c.getCouponId())).findFirst().orElse(null);
        Coupon couponB = available.stream().filter(c -> "C2".equals(c.getCouponId())).findFirst().orElse(null);
        Coupon couponC = available.stream().filter(c -> "C3".equals(c.getCouponId())).findFirst().orElse(null);

        assertTrue(couponA != null && couponA.getChecked());
        assertFalse(couponB != null && couponB.getChecked());
        assertTrue(couponC != null && couponC.getChecked());
    }

    @Test
    @DisplayName("场景6: 券溢出检测 - 30元商品 + 50元券(无门槛) → 实际优惠30(截断) → 总付0")
    void testCouponOverflow() {
        List<CartItem> items = Collections.singletonList(
                CartItem.builder().skuCode("SKU001").price(new BigDecimal("30")).quantity(1).build()
        );
        List<Coupon> coupons = Collections.singletonList(
                Coupon.builder().couponId("C1").name("50元券")
                        .couponType(CouponType.PRODUCT)
                        .faceValue(new BigDecimal("50"))
                        .expireDate(LocalDate.of(2026, 12, 31)).build()
        );

        OrchestrationResult result = orderCalcService.calculate(
                items, null, coupons, BigDecimal.ZERO);

        // 实际优惠被截断为30（不超过商品价格）
        assertEquals(0, new BigDecimal("30").compareTo(result.getProductCouponDiscount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalPayPrice()));

        // 验证实际抵扣金额
        Coupon checked = result.getCouponResult().getAvailableCoupons().get(0);
        assertTrue(checked.getChecked());
        assertEquals(0, new BigDecimal("30").compareTo(checked.getActualDiscount()));
    }
}
