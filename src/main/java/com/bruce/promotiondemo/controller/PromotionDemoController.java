package com.bruce.promotiondemo.controller;

import com.bruce.promotiondemo.model.*;
import com.bruce.promotiondemo.service.PromotionCompareService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 促销演示 Controller
 */
@RestController
@RequestMapping("/api/promotion-demo")
public class PromotionDemoController {

    private final PromotionCompareService compareService = new PromotionCompareService();

    /**
     * 对比水平/垂直叠加
     * 场景：商品200元，规则A商品8折(-40)，规则B满200减20
     * 水平叠加：200 - 40 - 20 = 140
     * 垂直叠加：200 → 160(8折后不满200) → 160
     */
    @PostMapping("/compare/horizontal-vs-vertical")
    public Map<String, Object> compareHorizontalVsVertical(@RequestBody(required = false) Map<String, Object> request) {
        Cart cart;
        List<Rule> rules;

        if (request == null || request.isEmpty()) {
            // 使用默认场景
            cart = createDefaultCart(new BigDecimal("200"));
            rules = createHorizontalVsVerticalRules();
        } else {
            cart = parseCart(request);
            rules = parseRules(request);
        }

        return compareService.compareHorizontalVsVertical(cart, rules);
    }

    /**
     * 演示回滚 bug
     * 场景：商品300元
     * 规则A(R1)：打9折(-30，priority=1)
     * 规则C(R3)：满260减20(priority=2，在A和B之间执行)
     * 规则B(R2)：打8折(-60，priority=3，与A互斥)
     *
     * 旧架构 bug：按 priority 顺序执行 + 回滚只标记 invalid，不会重算已执行下游规则的 basePrice。
     * A先执行→C基于270触发→回滚A选B→C仍保留(错误)。
     *
     * 新架构：穷举组合，且按“折扣先算、再算满减/直减”的阶段化顺序计算，选择B后价格为240<260，C不触发。
     */
    @PostMapping("/compare/rollback-bug")
    public Map<String, Object> demonstrateRollbackBug(@RequestBody(required = false) Map<String, Object> request) {
        Cart cart;
        List<Rule> rules;

        if (request == null || request.isEmpty()) {
            cart = createDefaultCart(new BigDecimal("300"));
            rules = createRollbackBugRules();
        } else {
            cart = parseCart(request);
            rules = parseRules(request);
        }

        return compareService.demonstrateRollbackBug(cart, rules);
    }

    /**
     * 对比贪心/穷举
     * 场景：商品100元×2件=200元
     * 规则A：满100减15（可叠加）
     * 规则B：满150减25（与A互斥）
     */
    @PostMapping("/compare/greedy-vs-exhaustive")
    public Map<String, Object> compareGreedyVsExhaustive(@RequestBody(required = false) Map<String, Object> request) {
        Cart cart;
        List<Rule> rules;

        if (request == null || request.isEmpty()) {
            cart = Cart.builder()
                    .items(Collections.singletonList(
                            CartItem.builder()
                                    .skuCode("SKU001")
                                    .price(new BigDecimal("100"))
                                    .quantity(2)
                                    .build()
                    ))
                    .build();
            rules = createGreedyVsExhaustiveRules();
        } else {
            cart = parseCart(request);
            rules = parseRules(request);
        }

        return compareService.compareGreedyVsExhaustive(cart, rules);
    }

    /**
     * 使用旧引擎计算
     */
    @PostMapping("/calculate/old")
    public Map<String, Object> calculateWithOldEngine(
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "false") boolean parallelCalculate) {

        Cart cart = parseCart(request);
        List<Rule> rules = parseRules(request);

        return compareService.calculateWithOldEngine(cart, rules, parallelCalculate);
    }

    /**
     * 使用新引擎计算
     */
    @PostMapping("/calculate/new")
    public Map<String, Object> calculateWithNewEngine(@RequestBody Map<String, Object> request) {
        Cart cart = parseCart(request);
        List<Rule> rules = parseRules(request);

        return compareService.calculateWithNewEngine(cart, rules);
    }

    // ========== 默认场景数据 ==========

    private Cart createDefaultCart(BigDecimal price) {
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

    /**
     * 水平 vs 垂直叠加场景的规则
     */
    private List<Rule> createHorizontalVsVerticalRules() {
        List<Rule> rules = new ArrayList<>();

        rules.add(Rule.builder()
                .id("R1")
                .name("商品8折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.8"))
                .priority(1)
                .build());

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

    /**
     * 回滚 bug 场景的规则
     * 优先级顺序：R1(9折) -> R3(满260减20) -> R2(8折)
     * R3 在 R1 和 R2 之间执行，是展示 bug 的关键
     */
    private List<Rule> createRollbackBugRules() {
        List<Rule> rules = new ArrayList<>();

        // R1：9折，与R2互斥，优先级1
        Set<String> exclusiveSet = new HashSet<>();
        exclusiveSet.add("R2");

        rules.add(Rule.builder()
                .id("R1")
                .name("商品9折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.9"))
                .priority(1)
                .exclusiveRuleIds(exclusiveSet)
                .build());

        // R3：满260减20，优先级2（在R1和R2之间）
        // 270(R1执行后) >= 260 会触发，但 240(R2执行后) < 260 不应触发
        rules.add(Rule.builder()
                .id("R3")
                .name("满260减20")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("260"))
                .discount(new BigDecimal("20"))
                .priority(2)
                .build());

        // R2：8折，与R1互斥，优先级3
        Set<String> exclusiveSet2 = new HashSet<>();
        exclusiveSet2.add("R1");

        rules.add(Rule.builder()
                .id("R2")
                .name("商品8折")
                .type(RuleType.DISCOUNT)
                .discount(new BigDecimal("0.8"))
                .priority(3)
                .exclusiveRuleIds(exclusiveSet2)
                .build());

        return rules;
    }

    /**
     * 贪心 vs 穷举场景的规则
     */
    private List<Rule> createGreedyVsExhaustiveRules() {
        List<Rule> rules = new ArrayList<>();

        Set<String> exclusiveSet = new HashSet<>();
        exclusiveSet.add("R2");

        rules.add(Rule.builder()
                .id("R1")
                .name("满100减15")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("100"))
                .discount(new BigDecimal("15"))
                .priority(1)
                .exclusiveRuleIds(exclusiveSet)
                .build());

        Set<String> exclusiveSet2 = new HashSet<>();
        exclusiveSet2.add("R1");

        rules.add(Rule.builder()
                .id("R2")
                .name("满150减25")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("150"))
                .discount(new BigDecimal("25"))
                .priority(2)
                .exclusiveRuleIds(exclusiveSet2)
                .build());

        return rules;
    }

    // ========== 请求解析 ==========

    @SuppressWarnings("unchecked")
    private Cart parseCart(Map<String, Object> request) {
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
        List<CartItem> items = new ArrayList<>();

        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                items.add(CartItem.builder()
                        .skuCode((String) itemData.get("skuCode"))
                        .price(new BigDecimal(itemData.get("price").toString()))
                        .quantity(((Number) itemData.get("quantity")).intValue())
                        .build());
            }
        }

        return Cart.builder().items(items).build();
    }

    @SuppressWarnings("unchecked")
    private List<Rule> parseRules(Map<String, Object> request) {
        List<Map<String, Object>> rulesData = (List<Map<String, Object>>) request.get("rules");
        List<Rule> rules = new ArrayList<>();

        if (rulesData != null) {
            for (Map<String, Object> ruleData : rulesData) {
                Set<String> exclusiveRuleIds = new HashSet<>();
                List<String> exclusiveList = (List<String>) ruleData.get("exclusiveRuleIds");
                if (exclusiveList != null) {
                    exclusiveRuleIds.addAll(exclusiveList);
                }

                Rule.RuleBuilder builder = Rule.builder()
                        .id((String) ruleData.get("id"))
                        .name((String) ruleData.get("name"))
                        .type(RuleType.valueOf((String) ruleData.get("type")))
                        .priority(((Number) ruleData.get("priority")).intValue())
                        .exclusiveRuleIds(exclusiveRuleIds);

                if (ruleData.get("threshold") != null) {
                    builder.threshold(new BigDecimal(ruleData.get("threshold").toString()));
                }
                if (ruleData.get("discount") != null) {
                    builder.discount(new BigDecimal(ruleData.get("discount").toString()));
                }

                rules.add(builder.build());
            }
        }

        return rules;
    }
}
