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
 * - 规则 A(R1)：打 9 折（-30，priority=1）
 * - 规则 C(R3)：订单满 260 减 20（priority=2，在A和B之间执行）
 * - 规则 B(R2)：打 8 折（-60，priority=3，与 A 互斥）
 *
 * 正确链路（新架构的组合计算语义）：
 * - 折扣类先算，再算满减/直减（阶段化执行）
 * - 若最终选择 R2(8折)，则：300 -> 240，240 < 260，所以 R3 不应触发
 *
 * 旧架构执行过程（有 bug，按 priority 执行 + 回滚不重算下游）：
 * 1. A 执行：300 → 270，减30
 * 2. C 执行：270 >= 260，触发，减20 → 250
 * 3. B 执行：发现与 A 互斥，B 的优惠(60) > A 的优惠(30)
 * 4. 回滚 A：将 A 的 detail 标记为 invalid，currentPrice 从 250 变回 280（只撤销了A的-30，C仍保留）
 * 5. B 基于 280 计算，减 56 → 224
 * 6. 【BUG】C 的 detail 仍然有效（条件是基于 270>=260 判断的）
 *    但正确的做法是：B(8折)=240 < 260，C 不应该被触发
 *
 * 旧架构结果（有bug）：额外叠加了不该触发的 R3，导致应付更低（多减了20）
 * 新架构结果（正确）：只应用 R2，应付=240，优惠=60
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

        // 旧架构有 bug：回滚 A 后，C 的 detail 仍然有效
        // 而正确的做法是：B(8折) 执行后价格变为 240，240 < 260，C 不应该被应用
        Optional<ReductionDetail> detailC = result.getReductionDetails().stream()
                .filter(d -> d.getRuleId().equals("R3") && d.getValid())
                .findFirst();

        // 【Bug 证据】C 仍然被应用了，因为它的条件判断基于回滚前的价格 270
        assertTrue(detailC.isPresent(), "【Bug证明】规则C被错误地应用了（正确情况下B执行后240<260，C不应触发）");

        System.out.println("【Bug 证据】");
        System.out.println("规则C的basePrice = " + detailC.get().getBasePrice());
        System.out.println("C被应用了，因为它的条件是基于270判断的（270>=260）");
        System.out.println("正确情况：选择B(8折)后价格为240，240<260，C不应该被应用");
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

        // 新架构会独立计算所有组合，并按“折扣先算、再算满减/直减”的阶段化顺序执行：
        // [R1]: 300 → 270，优惠30
        // [R2]: 300 → 240，优惠60（最优）
        // [R3]: 300 → 280(满260减20)，优惠20
        // [R1,R3]: 300 → 270(9折) → 250(270>=260,减20)，优惠50
        // [R3,R2]: 300 → 240(8折) → (240<260, R3不触发)，优惠60

        // 验证新架构选择了最优结果：只应用 R2
        assertEquals(new BigDecimal("60.00"), result.getTotalDiscount());
        assertEquals(Collections.singletonList("R2"), result.getAppliedRuleIds());

        // 关键验证：R3 不应触发
        assertFalse(result.getReductionDetails().stream()
                        .anyMatch(d -> d.getRuleId().equals("R3") && Boolean.TRUE.equals(d.getValid())),
                "新架构下选择R2后价格为240<260，R3不应触发");
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
        System.out.println("旧架构因回滚bug，C规则仍按270>=260被错误触发（正确应按240判断，不触发）");
        System.out.println("新架构穷举组合且阶段化执行，避免回滚导致的下游规则不重算问题");
        System.out.println("==========================================");

        // 断言：旧架构会把 R3 错误保留为有效，新架构不会
        assertTrue(oldResult.getReductionDetails().stream()
                        .anyMatch(d -> d.getRuleId().equals("R3") && Boolean.TRUE.equals(d.getValid())),
                "旧架构应复现bug：回滚后R3仍为有效");
        assertFalse(newResult.getReductionDetails().stream()
                        .anyMatch(d -> d.getRuleId().equals("R3") && Boolean.TRUE.equals(d.getValid())),
                "新架构应避免该bug：选择R2后R3不触发");
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

        // 规则A：9折，与B互斥，优先级1
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

        // 规则C：满260减20，优先级2（在A和B之间执行，这是展示bug的关键）
        // 270(A执行后) >= 260，会触发
        // 但 240(B执行后) < 260，正确情况不应触发
        rules.add(Rule.builder()
                .id("R3")
                .name("满260减20")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("260"))
                .discount(new BigDecimal("20"))
                .priority(2)
                .build());

        // 规则B：8折，与A互斥，优先级3
        Set<String> exclusiveSetB = new HashSet<>();
        exclusiveSetB.add("R1");

        rules.add(Rule.builder()
                .id("R2")
                .name("商品8折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.8"))
                .priority(3)
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
