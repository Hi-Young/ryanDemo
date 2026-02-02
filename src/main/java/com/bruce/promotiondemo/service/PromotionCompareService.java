package com.bruce.promotiondemo.service;

import com.bruce.promotiondemo.engine.NewPromotionEngine;
import com.bruce.promotiondemo.engine.OldPromotionEngine;
import com.bruce.promotiondemo.model.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 促销计算对比服务
 */
public class PromotionCompareService {

    private final OldPromotionEngine oldEngineHorizontal = new OldPromotionEngine(true);
    private final OldPromotionEngine oldEngineVertical = new OldPromotionEngine(false);
    private final NewPromotionEngine newEngine = new NewPromotionEngine();

    /**
     * 对比水平叠加与垂直叠加
     */
    public Map<String, Object> compareHorizontalVsVertical(Cart cart, List<Rule> rules) {
        Cart horizontalResult = oldEngineHorizontal.calculate(cart.copy(), rules);
        Cart verticalResult = oldEngineVertical.calculate(cart.copy(), rules);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "水平叠加 vs 垂直叠加");
        result.put("originalPrice", cart.getTotalPrice());
        result.put("horizontal", buildResultMap(horizontalResult, "水平叠加"));
        result.put("vertical", buildResultMap(verticalResult, "垂直叠加"));
        result.put("explanation", buildHorizontalVsVerticalExplanation(cart, rules, horizontalResult, verticalResult));

        return result;
    }

    /**
     * 演示回滚 bug
     */
    public Map<String, Object> demonstrateRollbackBug(Cart cart, List<Rule> rules) {
        Cart oldResult = oldEngineVertical.calculate(cart.copy(), rules);
        Cart newResult = newEngine.calculate(cart.copy(), rules);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "回滚 Bug 演示");
        result.put("originalPrice", cart.getTotalPrice());
        result.put("oldEngine", buildResultMap(oldResult, "旧架构(有回滚bug)"));
        result.put("newEngine", buildResultMap(newResult, "新架构(穷举最优)"));
        result.put("explanation", buildRollbackBugExplanation(cart, rules, oldResult, newResult));

        return result;
    }

    /**
     * 对比贪心与穷举
     */
    public Map<String, Object> compareGreedyVsExhaustive(Cart cart, List<Rule> rules) {
        Cart greedyResult = oldEngineVertical.calculate(cart.copy(), rules);
        Cart exhaustiveResult = newEngine.calculate(cart.copy(), rules);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenario", "贪心选择 vs 穷举最优");
        result.put("originalPrice", cart.getTotalPrice());
        result.put("greedy", buildResultMap(greedyResult, "旧架构(贪心)"));
        result.put("exhaustive", buildResultMap(exhaustiveResult, "新架构(穷举)"));
        result.put("explanation", buildGreedyVsExhaustiveExplanation(cart, rules, greedyResult, exhaustiveResult));

        return result;
    }

    /**
     * 使用旧引擎计算
     */
    public Map<String, Object> calculateWithOldEngine(Cart cart, List<Rule> rules, boolean parallelCalculate) {
        OldPromotionEngine engine = parallelCalculate ? oldEngineHorizontal : oldEngineVertical;
        Cart result = engine.calculate(cart.copy(), rules);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("engine", engine.getName());
        response.put("originalPrice", cart.getTotalPrice());
        response.put("result", buildResultMap(result, engine.getName()));

        return response;
    }

    /**
     * 使用新引擎计算
     */
    public Map<String, Object> calculateWithNewEngine(Cart cart, List<Rule> rules) {
        Cart result = newEngine.calculate(cart.copy(), rules);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("engine", newEngine.getName());
        response.put("originalPrice", cart.getTotalPrice());
        response.put("result", buildResultMap(result, newEngine.getName()));

        return response;
    }

    private Map<String, Object> buildResultMap(Cart cart, String engineName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("engineName", engineName);
        map.put("payPrice", cart.getPayPrice());
        map.put("totalDiscount", cart.getTotalDiscount());
        map.put("appliedRules", cart.getAppliedRuleIds());
        map.put("details", buildDetailsMap(cart.getReductionDetails()));
        return map;
    }

    private List<Map<String, Object>> buildDetailsMap(List<ReductionDetail> details) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ReductionDetail detail : details) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("ruleId", detail.getRuleId());
            map.put("ruleName", detail.getRuleName());
            map.put("basePrice", detail.getBasePrice());
            map.put("reduction", detail.getReduction());
            map.put("calculatedPrice", detail.getCalculatedPrice());
            map.put("valid", detail.getValid());
            list.add(map);
        }
        return list;
    }

    private String buildHorizontalVsVerticalExplanation(Cart cart, List<Rule> rules,
                                                         Cart horizontalResult, Cart verticalResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("【水平叠加】所有规则基于原价").append(cart.getTotalPrice()).append("计算，");
        sb.append("总优惠=").append(horizontalResult.getTotalDiscount()).append("，");
        sb.append("应付=").append(horizontalResult.getPayPrice()).append("。\n");

        sb.append("【垂直叠加】每条规则基于上一条计算后的价格计算，");
        sb.append("总优惠=").append(verticalResult.getTotalDiscount()).append("，");
        sb.append("应付=").append(verticalResult.getPayPrice()).append("。\n");

        BigDecimal diff = horizontalResult.getTotalDiscount().subtract(verticalResult.getTotalDiscount());
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("水平叠加多优惠了").append(diff).append("元。");
        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
            sb.append("垂直叠加多优惠了").append(diff.negate()).append("元。");
        } else {
            sb.append("两种模式优惠相同。");
        }

        return sb.toString();
    }

    private String buildRollbackBugExplanation(Cart cart, List<Rule> rules,
                                                Cart oldResult, Cart newResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("【旧架构问题】当发生规则回滚时，已执行的下游规则的 basePrice 不会重算，导致规则被错误保留/错误命中。\n");
        sb.append("旧架构结果：应付=").append(oldResult.getPayPrice()).append("，优惠=").append(oldResult.getTotalDiscount()).append("\n");
        sb.append("新架构结果：应付=").append(newResult.getPayPrice()).append("，优惠=").append(newResult.getTotalDiscount()).append("\n");

        sb.append("\n【旧架构执行过程】\n");
        for (ReductionDetail detail : oldResult.getReductionDetails()) {
            sb.append("  - ").append(detail.getRuleName())
                    .append(": basePrice=").append(detail.getBasePrice())
                    .append(", 减").append(detail.getReduction())
                    .append(detail.getValid() ? " (有效)" : " (已回滚)")
                    .append("\n");
        }

        sb.append("\n【新架构】独立计算每个合法组合，按“折扣先算、再算满减/直减”的阶段化顺序执行，避免回滚带来的下游规则不重算问题。");

        return sb.toString();
    }

    private String buildGreedyVsExhaustiveExplanation(Cart cart, List<Rule> rules,
                                                       Cart greedyResult, Cart exhaustiveResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("【贪心策略】按优先级顺序执行，遇到互斥时通过预测判断是否回滚，可能陷入局部最优。\n");
        sb.append("【穷举策略】计算所有合法组合的结果，选择全局最优。\n\n");

        sb.append("贪心结果：应付=").append(greedyResult.getPayPrice())
                .append("，优惠=").append(greedyResult.getTotalDiscount())
                .append("，应用规则=").append(greedyResult.getAppliedRuleIds()).append("\n");

        sb.append("穷举结果：应付=").append(exhaustiveResult.getPayPrice())
                .append("，优惠=").append(exhaustiveResult.getTotalDiscount())
                .append("，应用规则=").append(exhaustiveResult.getAppliedRuleIds()).append("\n");

        BigDecimal diff = exhaustiveResult.getTotalDiscount().subtract(greedyResult.getTotalDiscount());
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("\n穷举比贪心多优惠了").append(diff).append("元，说明贪心陷入了局部最优。");
        } else if (diff.compareTo(BigDecimal.ZERO) == 0) {
            sb.append("\n两种策略结果相同。");
        }

        return sb.toString();
    }
}
