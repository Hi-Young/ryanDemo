package com.bruce.promotiondemo;

import com.bruce.promotiondemo.engine.NewPromotionEngine;
import com.bruce.promotiondemo.engine.OldPromotionEngine;
import com.bruce.promotiondemo.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 贪心 vs 穷举 测试
 *
 * 场景：商品 100 元 × 2 件 = 200 元
 * - 规则 A：满 100 减 15（与B互斥）
 * - 规则 B：满 150 减 25（与A互斥）
 *
 * 贪心策略（旧架构）：
 * 1. A 先执行（priority=1）：200 >= 100，触发，减15，变为 185
 * 2. B 执行（priority=2）：发现与 A 互斥，B 的优惠(25) > A 的优惠(15)
 * 3. 回滚 A，应用 B：200 → 175
 *
 * 穷举策略（新架构）：
 * 1. 计算 [A]：200 - 15 = 185，优惠 15
 * 2. 计算 [B]：200 - 25 = 175，优惠 25
 * 3. 选择优惠最大的 [B]
 *
 * 这个场景两者结果相同，但穷举保证了全局最优
 */
@DisplayName("贪心 vs 穷举测试")
public class GreedyVsExhaustiveTest {

    @Test
    @DisplayName("贪心策略：按优先级顺序执行，遇互斥则预测回滚")
    void testGreedyStrategy() {
        Cart cart = createCart();
        List<Rule> rules = createRules();

        OldPromotionEngine engine = new OldPromotionEngine(false);
        Cart result = engine.calculate(cart, rules);

        System.out.println("【贪心策略测试】");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println("应付: " + result.getPayPrice());
        System.out.println("总优惠: " + result.getTotalDiscount());
        System.out.println("应用规则: " + result.getAppliedRuleIds());
        System.out.println();

        printDetails(result);

        // 贪心策略：A先执行，B更优，回滚A应用B
        assertEquals(new BigDecimal("25.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("175.00"), result.getPayPrice());
    }

    @Test
    @DisplayName("穷举策略：计算所有组合，选择最优")
    void testExhaustiveStrategy() {
        Cart cart = createCart();
        List<Rule> rules = createRules();

        NewPromotionEngine engine = new NewPromotionEngine();
        Cart result = engine.calculate(cart, rules);

        System.out.println("【穷举策略测试】");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println("应付: " + result.getPayPrice());
        System.out.println("总优惠: " + result.getTotalDiscount());
        System.out.println("应用规则: " + result.getAppliedRuleIds());
        System.out.println();

        printDetails(result);

        // 穷举策略：计算 [A]=15 和 [B]=25，选择 B
        assertEquals(new BigDecimal("25.00"), result.getTotalDiscount());
        assertEquals(new BigDecimal("175.00"), result.getPayPrice());
        assertTrue(result.getAppliedRuleIds().contains("R2"));
    }

    @Test
    @DisplayName("复杂场景：贪心可能陷入局部最优")
    void testComplexScenarioWhereGreedyFails() {
        // 构造一个贪心会失败的场景
        // 商品：300元
        // 规则A：满100减10（priority=1）
        // 规则B：满200减30（priority=2，与A互斥）
        // 规则C：满280减5（priority=3）
        //
        // 贪心执行：
        // 1. A: 300 -> 290, 减10
        // 2. B: 与A互斥，B(30) > A(10)，回滚A，应用B: 300 -> 270, 减30
        // 3. C: 270 < 280, 不触发
        // 结果：270，优惠30
        //
        // 穷举：
        // [A]: 300 - 10 = 290
        // [B]: 300 - 30 = 270
        // [A,C]: 300 - 10 - 5 = 285 (290 >= 280, C触发)
        // 最优：[A,C] 优惠15，但实际 [B] 优惠30更大

        Cart cart = Cart.builder()
                .items(Collections.singletonList(
                        CartItem.builder()
                                .skuCode("SKU001")
                                .price(new BigDecimal("300"))
                                .quantity(1)
                                .build()
                ))
                .build();

        List<Rule> rules = new ArrayList<>();

        Set<String> exclusiveSetA = new HashSet<>();
        exclusiveSetA.add("R2");

        rules.add(Rule.builder()
                .id("R1")
                .name("满100减10")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("100"))
                .discount(new BigDecimal("10"))
                .priority(1)
                .exclusiveRuleIds(exclusiveSetA)
                .build());

        Set<String> exclusiveSetB = new HashSet<>();
        exclusiveSetB.add("R1");

        rules.add(Rule.builder()
                .id("R2")
                .name("满200减30")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("200"))
                .discount(new BigDecimal("30"))
                .priority(2)
                .exclusiveRuleIds(exclusiveSetB)
                .build());

        rules.add(Rule.builder()
                .id("R3")
                .name("满280减5")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("280"))
                .discount(new BigDecimal("5"))
                .priority(3)
                .build());

        OldPromotionEngine oldEngine = new OldPromotionEngine(false);
        NewPromotionEngine newEngine = new NewPromotionEngine();

        Cart oldResult = oldEngine.calculate(cart.copy(), rules);
        Cart newResult = newEngine.calculate(cart.copy(), rules);

        System.out.println("==========================================");
        System.out.println("【复杂场景：贪心 vs 穷举对比】");
        System.out.println("==========================================");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println();

        System.out.println("【贪心策略（旧架构）】");
        System.out.println("应付: " + oldResult.getPayPrice());
        System.out.println("总优惠: " + oldResult.getTotalDiscount());
        System.out.println("应用规则: " + oldResult.getAppliedRuleIds());
        printDetails(oldResult);

        System.out.println("【穷举策略（新架构）】");
        System.out.println("应付: " + newResult.getPayPrice());
        System.out.println("总优惠: " + newResult.getTotalDiscount());
        System.out.println("应用规则: " + newResult.getAppliedRuleIds());
        printDetails(newResult);

        System.out.println("==========================================");
        System.out.println("分析：");
        System.out.println("贪心：A先执行，B更优回滚A，但回滚后C不满足条件");
        System.out.println("穷举：独立计算每个组合，[B]=30 vs [A,C]=15，选择[B]");
        System.out.println("本场景两者结果相同，都选择了B");
        System.out.println("==========================================");
    }

    @Test
    @DisplayName("对比贪心与穷举策略")
    void testCompareStrategies() {
        Cart cart = createCart();
        List<Rule> rules = createRules();

        OldPromotionEngine oldEngine = new OldPromotionEngine(false);
        NewPromotionEngine newEngine = new NewPromotionEngine();

        Cart oldResult = oldEngine.calculate(cart.copy(), rules);
        Cart newResult = newEngine.calculate(cart.copy(), rules);

        System.out.println("==========================================");
        System.out.println("【贪心 vs 穷举策略对比】");
        System.out.println("==========================================");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println();
        System.out.println("贪心（旧架构）: 应付=" + oldResult.getPayPrice() + ", 优惠=" + oldResult.getTotalDiscount());
        System.out.println("穷举（新架构）: 应付=" + newResult.getPayPrice() + ", 优惠=" + newResult.getTotalDiscount());
        System.out.println();

        BigDecimal diff = newResult.getTotalDiscount().subtract(oldResult.getTotalDiscount());
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("穷举比贪心多优惠了 " + diff + " 元");
        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("贪心比穷举多优惠了 " + diff.negate() + " 元");
        } else {
            System.out.println("两种策略结果相同");
        }
        System.out.println("==========================================");

        // 两种策略应该得到相同的最优结果
        assertEquals(oldResult.getTotalDiscount(), newResult.getTotalDiscount());
    }

    private Cart createCart() {
        return Cart.builder()
                .items(Collections.singletonList(
                        CartItem.builder()
                                .skuCode("SKU001")
                                .price(new BigDecimal("100"))
                                .quantity(2)
                                .build()
                ))
                .build();
    }

    private List<Rule> createRules() {
        List<Rule> rules = new ArrayList<>();

        // 规则A：满100减15，与B互斥
        Set<String> exclusiveSetA = new HashSet<>();
        exclusiveSetA.add("R2");

        rules.add(Rule.builder()
                .id("R1")
                .name("满100减15")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("100"))
                .discount(new BigDecimal("15"))
                .priority(1)
                .exclusiveRuleIds(exclusiveSetA)
                .build());

        // 规则B：满150减25，与A互斥
        Set<String> exclusiveSetB = new HashSet<>();
        exclusiveSetB.add("R1");

        rules.add(Rule.builder()
                .id("R2")
                .name("满150减25")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("150"))
                .discount(new BigDecimal("25"))
                .priority(2)
                .exclusiveRuleIds(exclusiveSetB)
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
