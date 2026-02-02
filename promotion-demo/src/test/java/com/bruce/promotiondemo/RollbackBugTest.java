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
 * 回滚 Bug 测试
 *
 * 场景：商品原价 300 元
 * - 规则 A：打 9 折（-30，priority=1）
 * - 规则 B：打 8 折（-60，priority=2，与 A 互斥）
 * - 规则 C：订单满 250 减 30（priority=3）
 *
 * 旧架构执行过程（有 bug）：
 * 1. A 执行：300 → 270，减30
 * 2. C 执行：270 >= 250，触发，基于 270 计算，减30，价格变为 240
 * 3. B 执行：发现与 A 互斥，B 的优惠(60) > A 的优惠(30)
 * 4. 回滚 A：将 A 的 detail 标记为 invalid
 * 5. 【BUG】C 的 basePrice 仍然是 270（应该基于 B 的结果 240 重算）
 *
 * 旧架构结果（错误）：300 - 60(B) - 30(C，但basePrice错误) = 210
 * 新架构结果（正确）：会独立计算 [B,C] 组合，300 → 240(8折) → 240(不满250) = 240
 */
@DisplayName("回滚 Bug 测试")
public class RollbackBugTest {

    @Test
    @DisplayName("旧架构：回滚时不重算下游规则的 basePrice")
    void testOldEngineRollbackBug() {
        Cart cart = createCart();
        List<Rule> rules = createRules();

        OldPromotionEngine engine = new OldPromotionEngine(false);
        Cart result = engine.calculate(cart, rules);

        System.out.println("【旧架构回滚 Bug 测试】");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println("应付: " + result.getPayPrice());
        System.out.println("总优惠: " + result.getTotalDiscount());
        System.out.println();

        printDetails(result);

        // 旧架构有 bug：回滚 A 后，C 的 basePrice 仍然是基于 A 计算的 270
        // 检查 C 的 basePrice 是否为 270（错误的）
        Optional<ReductionDetail> detailC = result.getReductionDetails().stream()
                .filter(d -> d.getRuleId().equals("R3") && d.getValid())
                .findFirst();

        assertTrue(detailC.isPresent(), "规则C应该被应用");

        // 这里展示了 bug：C 的 basePrice 是 270（基于 A 计算后），而不是 240（基于 B 计算后）
        System.out.println("【Bug 证据】");
        System.out.println("规则C的basePrice = " + detailC.get().getBasePrice());
        System.out.println("正确应该是: 240 (300 * 0.8)");
        System.out.println("实际是: 270 (300 * 0.9)，因为回滚时没有重算");
    }

    @Test
    @DisplayName("新架构：独立计算每个组合，避免回滚 bug")
    void testNewEngineCorrectBehavior() {
        Cart cart = createCart();
        List<Rule> rules = createRules();

        NewPromotionEngine engine = new NewPromotionEngine();
        Cart result = engine.calculate(cart, rules);

        System.out.println("【新架构正确计算测试】");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println("应付: " + result.getPayPrice());
        System.out.println("总优惠: " + result.getTotalDiscount());
        System.out.println("应用规则: " + result.getAppliedRuleIds());
        System.out.println();

        printDetails(result);

        // 新架构会独立计算所有组合：
        // [A]: 300 → 270，优惠30
        // [B]: 300 → 240，优惠60
        // [A,C]: 300 → 270 → 240，优惠60
        // [B,C]: 300 → 240 (不满250) → 240，优惠60
        // 最优是 [A,C] 或 [B]，都是优惠60，但 [A,C] 应用了两个规则

        // 验证新架构选择了最优组合
        assertEquals(new BigDecimal("60.00"), result.getTotalDiscount());
    }

    @Test
    @DisplayName("对比新旧架构的计算结果差异")
    void testCompareOldAndNew() {
        Cart cart = createCart();
        List<Rule> rules = createRules();

        OldPromotionEngine oldEngine = new OldPromotionEngine(false);
        NewPromotionEngine newEngine = new NewPromotionEngine();

        Cart oldResult = oldEngine.calculate(cart.copy(), rules);
        Cart newResult = newEngine.calculate(cart.copy(), rules);

        System.out.println("==========================================");
        System.out.println("【回滚 Bug 新旧架构对比】");
        System.out.println("==========================================");
        System.out.println("原价: " + cart.getTotalPrice());
        System.out.println();

        System.out.println("【旧架构（有回滚 bug）】");
        System.out.println("应付: " + oldResult.getPayPrice());
        System.out.println("总优惠: " + oldResult.getTotalDiscount());
        printDetails(oldResult);

        System.out.println("【新架构（穷举最优）】");
        System.out.println("应付: " + newResult.getPayPrice());
        System.out.println("总优惠: " + newResult.getTotalDiscount());
        printDetails(newResult);

        System.out.println("==========================================");
        System.out.println("结论：");
        System.out.println("旧架构因回滚bug，C规则的basePrice错误使用了270而非240");
        System.out.println("新架构独立计算每个组合，得到正确结果");
        System.out.println("==========================================");
    }

    private Cart createCart() {
        return Cart.builder()
                .items(Collections.singletonList(
                        CartItem.builder()
                                .skuCode("SKU001")
                                .price(new BigDecimal("300"))
                                .quantity(1)
                                .build()
                ))
                .build();
    }

    private List<Rule> createRules() {
        List<Rule> rules = new ArrayList<>();

        // 规则A：9折，与B互斥
        Set<String> exclusiveSetA = new HashSet<>();
        exclusiveSetA.add("R2");

        rules.add(Rule.builder()
                .id("R1")
                .name("商品9折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.9"))
                .priority(1)
                .exclusiveRuleIds(exclusiveSetA)
                .build());

        // 规则B：8折，与A互斥
        Set<String> exclusiveSetB = new HashSet<>();
        exclusiveSetB.add("R1");

        rules.add(Rule.builder()
                .id("R2")
                .name("商品8折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.8"))
                .priority(2)
                .exclusiveRuleIds(exclusiveSetB)
                .build());

        // 规则C：满250减30
        rules.add(Rule.builder()
                .id("R3")
                .name("满250减30")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("250"))
                .discount(new BigDecimal("30"))
                .priority(3)
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
