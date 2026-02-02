package com.bruce.promotiondemo.engine;

import com.bruce.promotiondemo.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 旧架构促销引擎
 * 特点：
 * 1. 支持 parallelCalculate 参数切换水平/垂直叠加
 * 2. 规则按 priority 顺序执行
 * 3. 互斥规则用 PredictStrategy（预测策略）判断是否回滚
 * 4. 【BUG】回滚时只标记 invalid，不重算下游 basePrice
 */
public class OldPromotionEngine implements PromotionEngine {

    /**
     * 是否水平叠加模式
     * true: 所有规则基于原价计算（水平叠加）
     * false: 每条规则基于上一条规则计算后的价格计算（垂直叠加）
     */
    private final boolean parallelCalculate;

    public OldPromotionEngine(boolean parallelCalculate) {
        this.parallelCalculate = parallelCalculate;
    }

    @Override
    public String getName() {
        return parallelCalculate ? "旧架构(水平叠加)" : "旧架构(垂直叠加)";
    }

    @Override
    public Cart calculate(Cart cart, List<Rule> rules) {
        Cart result = cart.copy();

        // 按优先级排序
        List<Rule> sortedRules = rules.stream()
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .collect(Collectors.toList());

        BigDecimal originalPrice = result.getTotalPrice();
        BigDecimal currentPrice = originalPrice;

        for (Rule rule : sortedRules) {
            // 计算基准价：水平叠加用原价，垂直叠加用当前价
            BigDecimal basePrice = parallelCalculate ? originalPrice : currentPrice;

            // 检查规则是否满足条件
            if (!checkRuleCondition(rule, basePrice)) {
                continue;
            }

            // 计算优惠
            BigDecimal reduction = calculateReduction(rule, basePrice);
            if (reduction.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 检查互斥规则，决定是否回滚
            RollbackResult rollbackResult = handleExclusiveRules(result, rule, reduction, originalPrice);
            if (!rollbackResult.shouldApply) {
                continue;
            }

            // 如果发生了回滚，需要调整 currentPrice
            // 【BUG 模拟】回滚时只调整 currentPrice，不重算已执行规则的 basePrice
            if (rollbackResult.rollbackReduction.compareTo(BigDecimal.ZERO) > 0) {
                currentPrice = currentPrice.add(rollbackResult.rollbackReduction);
                // 重新基于回滚后的 currentPrice 计算当前规则
                basePrice = parallelCalculate ? originalPrice : currentPrice;
                reduction = calculateReduction(rule, basePrice);
            }

            // 应用规则
            BigDecimal newPrice = currentPrice.subtract(reduction);
            ReductionDetail detail = ReductionDetail.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getName())
                    .basePrice(basePrice)
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
     * 回滚结果
     */
    private static class RollbackResult {
        boolean shouldApply;
        BigDecimal rollbackReduction;

        RollbackResult(boolean shouldApply, BigDecimal rollbackReduction) {
            this.shouldApply = shouldApply;
            this.rollbackReduction = rollbackReduction;
        }
    }

    /**
     * 检查规则条件是否满足
     */
    private boolean checkRuleCondition(Rule rule, BigDecimal price) {
        if (rule.getType() == RuleType.THRESHOLD_AMOUNT_OFF) {
            return price.compareTo(rule.getThreshold()) >= 0;
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
     * 处理互斥规则（模拟旧架构的 PredictStrategy）
     * 【BUG所在】回滚时只标记 invalid，不重算下游规则的 basePrice
     *
     * @return 回滚结果，包含是否应用和回滚的优惠金额
     */
    private RollbackResult handleExclusiveRules(Cart cart, Rule currentRule, BigDecimal currentReduction, BigDecimal originalPrice) {
        List<ReductionDetail> existingDetails = cart.getReductionDetails();
        List<Rule> appliedRules = cart.getAppliedRules();

        // 找出与当前规则互斥的已应用规则
        List<Rule> conflictRules = appliedRules.stream()
                .filter(r -> currentRule.isExclusiveWith(r))
                .collect(Collectors.toList());

        if (conflictRules.isEmpty()) {
            return new RollbackResult(true, BigDecimal.ZERO);
        }

        // 计算已应用的互斥规则的总优惠
        BigDecimal existingReduction = BigDecimal.ZERO;
        for (Rule conflictRule : conflictRules) {
            for (ReductionDetail detail : existingDetails) {
                if (detail.getRuleId().equals(conflictRule.getId()) && detail.getValid()) {
                    existingReduction = existingReduction.add(detail.getReduction());
                }
            }
        }

        // PredictStrategy：如果当前规则优惠更大，回滚之前的规则
        if (currentReduction.compareTo(existingReduction) > 0) {
            // 【BUG】只标记 invalid，不重算下游规则的 basePrice
            for (Rule conflictRule : conflictRules) {
                for (ReductionDetail detail : existingDetails) {
                    if (detail.getRuleId().equals(conflictRule.getId())) {
                        detail.setValid(false);
                    }
                }
            }
            return new RollbackResult(true, existingReduction);
        }

        // 当前规则优惠不大，不应用
        return new RollbackResult(false, BigDecimal.ZERO);
    }
}
