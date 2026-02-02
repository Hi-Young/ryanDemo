package com.bruce.promotiondemo.engine;

import com.bruce.promotiondemo.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新架构促销引擎
 * 特点：
 * 1. 固定垂直叠加模式
 * 2. 先过滤出所有能命中的规则
 * 3. 生成所有合法组合（排除互斥）
 * 4. 每个组合独立计算（从原始 Cart 重建）
 * 5. 取 totalDiscount 最大的组合
 */
public class NewPromotionEngine implements PromotionEngine {

    @Override
    public String getName() {
        return "新架构(穷举最优)";
    }

    @Override
    public Cart calculate(Cart cart, List<Rule> rules) {
        BigDecimal originalPrice = cart.getTotalPrice();

        // 1. 过滤出基于原价能命中的规则
        List<Rule> matchedRules = rules.stream()
                .filter(r -> checkRuleCondition(r, originalPrice))
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .collect(Collectors.toList());

        if (matchedRules.isEmpty()) {
            return cart.copy();
        }

        // 2. 生成所有合法组合（排除互斥）
        List<List<Rule>> validCombinations = generateValidCombinations(matchedRules);

        // 3. 计算每个组合的结果，找出最优
        Cart bestResult = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (List<Rule> combination : validCombinations) {
            Cart result = calculateCombination(cart, combination);
            BigDecimal discount = result.getTotalDiscount();

            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                bestResult = result;
            }
        }

        return bestResult != null ? bestResult : cart.copy();
    }

    /**
     * 检查规则条件是否满足（基于原价）
     */
    private boolean checkRuleCondition(Rule rule, BigDecimal price) {
        if (rule.getType() == RuleType.THRESHOLD_AMOUNT_OFF) {
            return price.compareTo(rule.getThreshold()) >= 0;
        }
        return true;
    }

    /**
     * 生成所有合法的规则组合（排除互斥规则同时出现的组合）
     */
    private List<List<Rule>> generateValidCombinations(List<Rule> rules) {
        List<List<Rule>> result = new ArrayList<>();
        generateCombinations(rules, 0, new ArrayList<>(), result);
        return result;
    }

    private void generateCombinations(List<Rule> rules, int index, List<Rule> current, List<List<Rule>> result) {
        if (index == rules.size()) {
            if (!current.isEmpty()) {
                result.add(new ArrayList<>(current));
            }
            return;
        }

        Rule rule = rules.get(index);

        // 检查当前规则是否与已选规则互斥
        boolean canAdd = current.stream().noneMatch(r -> r.isExclusiveWith(rule));

        // 不选当前规则
        generateCombinations(rules, index + 1, current, result);

        // 选当前规则（如果可以）
        if (canAdd) {
            current.add(rule);
            generateCombinations(rules, index + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * 计算单个组合的结果
     * 【关键】每个组合从原始购物车开始，独立计算
     */
    private Cart calculateCombination(Cart originalCart, List<Rule> combination) {
        Cart result = originalCart.copy();
        BigDecimal currentPrice = result.getTotalPrice();

        // 新架构在计算组合时使用“阶段化执行”以贴近真实促销链路：
        // 1) 先执行折扣类（DISCOUNT）
        // 2) 再执行满减/直减类（AMOUNT_OFF / THRESHOLD_AMOUNT_OFF）
        // 同一阶段内仍按 priority 排序（垂直叠加）。
        List<Rule> sortedRules = combination.stream()
                .sorted(Comparator
                        .comparingInt((Rule r) -> stageOf(r.getType()))
                        .thenComparingInt(Rule::getPriority))
                .collect(Collectors.toList());

        for (Rule rule : sortedRules) {
            // 垂直叠加：基于当前价格检查条件
            if (!checkRuleConditionForCurrent(rule, currentPrice)) {
                continue;
            }

            // 计算优惠
            BigDecimal reduction = calculateReduction(rule, currentPrice);
            if (reduction.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal newPrice = currentPrice.subtract(reduction);

            ReductionDetail detail = ReductionDetail.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getName())
                    .basePrice(currentPrice)
                    .reduction(reduction)
                    .calculatedPrice(newPrice)
                    .valid(true)
                    .build();

            result.getReductionDetails().add(detail);
            result.getAppliedRules().add(rule);
            currentPrice = newPrice;
        }

        return result;
    }

    /**
     * 检查规则条件（基于当前价格）
     */
    private boolean checkRuleConditionForCurrent(Rule rule, BigDecimal currentPrice) {
        if (rule.getType() == RuleType.THRESHOLD_AMOUNT_OFF) {
            return currentPrice.compareTo(rule.getThreshold()) >= 0;
        }
        return true;
    }

    /**
     * 计算优惠金额
     */
    private BigDecimal calculateReduction(Rule rule, BigDecimal basePrice) {
        switch (rule.getType()) {
            case DISCOUNT:
                // 折扣：原价 * (1 - 折扣率)
                BigDecimal discountRate = BigDecimal.ONE.subtract(rule.getDiscount());
                return basePrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

            case AMOUNT_OFF:
            case THRESHOLD_AMOUNT_OFF:
                // 直减/满减：直接减去优惠金额
                return rule.getDiscount().setScale(2, RoundingMode.HALF_UP);

            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 规则执行阶段：折扣先于满减/直减。
     */
    private int stageOf(RuleType type) {
        if (type == RuleType.DISCOUNT) {
            return 0;
        }
        return 1;
    }
}
