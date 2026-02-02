package com.bruce.promotiondemo;

import com.bruce.promotiondemo.engine.OldPromotionEngine;
import com.bruce.promotiondemo.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 水平叠加 vs 垂直叠加 测试
 *
 * 场景：商品原价 200 元
 * - 规则 A：商品打 8 折（-40）
 * - 规则 B：订单满 200 减 20
 *
 * 水平叠加：200 - 40 - 20 = 140（两个规则都基于原价200计算）
 * 垂直叠加：200 → 160（8折后不满200，规则B不触发）→ 160
 */
@DisplayName("水平叠加 vs 垂直叠加测试")
public class HorizontalVsVerticalTest {

    @Test
    @DisplayName("水平叠加：所有规则基于原价计算")
    void testHorizontalCalculation() {
        // 准备数据
        Cart cart = createCart(new BigDecimal("200"));
        List<Rule> rules = createRules();

        // 水平叠加引擎
        OldPromotionEngine engine = new OldPromotionEngine(true);
        Cart result = engine.calculate(cart, rules);

        // 验证结果
        System.out.println("【水平叠加测试】");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println("应付: " + result.getPayPrice());
        System.out.println("总优惠: " + result.getTotalDiscount());
        System.out.println("应用规则: " + result.getAppliedRuleIds());

        printDetails(result);

        // 水平叠加：200 - 40 - 20 = 140
        assertEquals(new BigDecimal("60.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("140.00"), result.getPayPrice());
        assertEquals(2, result.getAppliedRuleIds().size());
    }

    @Test
    @DisplayName("垂直叠加：每条规则基于上一条计算后的价格")
    void testVerticalCalculation() {
        // 准备数据
        Cart cart = createCart(new BigDecimal("200"));
        List<Rule> rules = createRules();

        // 垂直叠加引擎
        OldPromotionEngine engine = new OldPromotionEngine(false);
        Cart result = engine.calculate(cart, rules);

        // 验证结果
        System.out.println("【垂直叠加测试】");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println("应付: " + result.getPayPrice());
        System.out.println("总优惠: " + result.getTotalDiscount());
        System.out.println("应用规则: " + result.getAppliedRuleIds());

        printDetails(result);

        // 垂直叠加：200 → 160（8折），160 < 200，满减不触发
        assertEquals(new BigDecimal("40.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("160.00"), result.getPayPrice());
        assertEquals(1, result.getAppliedRuleIds().size());
        assertEquals("R1", result.getAppliedRuleIds().get(0));
    }

    @Test
    @DisplayName("对比水平与垂直叠加的差异")
    void testComparison() {
        Cart cart = createCart(new BigDecimal("200"));
        List<Rule> rules = createRules();

        OldPromotionEngine horizontalEngine = new OldPromotionEngine(true);
        OldPromotionEngine verticalEngine = new OldPromotionEngine(false);

        Cart horizontalResult = horizontalEngine.calculate(cart.copy(), rules);
        Cart verticalResult = verticalEngine.calculate(cart.copy(), rules);

        System.out.println("==========================================");
        System.out.println("【水平 vs 垂直叠加对比】");
        System.out.println("==========================================");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println();
        System.out.println("水平叠加: 应付=" + horizontalResult.getPayPrice() + ", 优惠=" + horizontalResult.getTotalDiscount());
        System.out.println("垂直叠加: 应付=" + verticalResult.getPayPrice() + ", 优惠=" + verticalResult.getTotalDiscount());
        System.out.println();
        System.out.println("差异: 水平叠加多优惠 " +
                horizontalResult.getTotalDiscount().subtract(verticalResult.getTotalDiscount()) + " 元");
        System.out.println("==========================================");

        // 水平叠加优惠更多
        assertTrue(horizontalResult.getTotalDiscount().compareTo(verticalResult.getTotalDiscount()) > 0);
    }

    private Cart createCart(BigDecimal price) {
        return Cart.builder()
                .items(Collections.singletonList(
                        CartItem.builder()
                                .skuCode("SKU001")
                                .price(price)
                                .quantity(1)
                                .build()
                ))
                .build();
    }

    private List<Rule> createRules() {
        List<Rule> rules = new ArrayList<>();

        // 规则A：商品8折
        rules.add(Rule.builder()
                .id("R1")
                .name("商品8折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.8"))
                .priority(1)
                .build());

        // 规则B：满200减20
        rules.add(Rule.builder()
                .id("R2")
                .name("满200减20")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("200"))
                .discount(new BigDecimal("20"))
                .priority(2)
                .build());

        return rules;
    }

    private void printDetails(Cart result) {
        System.out.println("优惠明细:");
        for (ReductionDetail detail : result.getReductionDetails()) {
            System.out.println("  - " + detail.getRuleName() +
                    ": basePrice=" + detail.getBasePrice() +
                    ", 减" + detail.getReduction() +
                    ", 计算后=" + detail.getCalculatedPrice() +
                    (detail.getValid() ? " (有效)" : " (已回滚)"));
        }
        System.out.println();
    }
}
