//package com.ryan.business.exercise;
//
//import com.ryan.business.entity.PromotionOrder;
//import com.ryan.business.entity.PromotionResult;
//import com.ryan.business.strategy.PromotionStrategy;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 🎯 练习1：组合促销策略
// * 
// * 任务：实现一个能够智能组合多种促销的策略
// * 目标：让你体验策略模式的组合威力
// */
//@Component
//public class ComboPromotionStrategy implements PromotionStrategy {
//    
//    // TODO: 注入所有促销策略（提示：使用List<PromotionStrategy>）
//    @Autowired
//    private List<PromotionStrategy> allStrategies;
//    
//    @Override
//    public PromotionResult calculate(PromotionOrder order) {
//        // TODO: 实现组合促销逻辑
//        
//        BigDecimal originalAmount = order.getOriginalAmount();
//        BigDecimal bestFinalAmount = originalAmount;
//        List<String> bestPromotions = new ArrayList<>();
//        StringBuilder bestDescription = new StringBuilder("组合促销：");
//        
//        // TODO: 1. 遍历所有策略，找到适用的策略
//        List<PromotionStrategy> applicableStrategies = new ArrayList<>();
//        for (PromotionStrategy strategy : allStrategies) {
//            // 排除自己，避免无限递归
//            if (strategy == this) {
//                continue;
//            }
//            
//            // TODO: 检查策略是否适用于当前订单
//            if (/* 在这里填写条件判断 */) {
//                applicableStrategies.add(strategy);
//            }
//        }
//        
//        // TODO: 2. 尝试不同的组合方式
//        // 方式1：单一策略（找最优的单个策略）
//        for (PromotionStrategy strategy : applicableStrategies) {
//            // TODO: 计算单个策略的结果
//            PromotionResult result = /* 在这里调用策略计算 */;
//            
//            // TODO: 比较是否更优（价格更低）
//            if (/* 在这里填写比较条件 */) {
//                bestFinalAmount = result.getFinalAmount();
//                bestPromotions = new ArrayList<>(result.getAppliedPromotions());
//                bestDescription = new StringBuilder("单一最优策略：" + strategy.getStrategyName() + "；");
//            }
//        }
//        
//        // 方式2：叠加策略（尝试叠加不同类型的促销）
//        // TODO: 实现策略叠加逻辑
//        // 提示：满减 + 会员折扣，新用户 + 打折等
//        
//        // TODO: 3. 返回最优结果
//        BigDecimal discountAmount = originalAmount.subtract(bestFinalAmount);
//        return new PromotionResult(
//                originalAmount,
//                bestFinalAmount,
//                discountAmount,
//                bestPromotions,
//                bestDescription.toString()
//        );
//    }
//    
//    @Override
//    public String getStrategyName() {
//        return "组合促销";
//    }
//    
//    @Override
//    public boolean supports(PromotionOrder order) {
//        // TODO: 组合策略的支持条件
//        // 提示：当存在多个适用策略时才启用组合策略
//        return /* 在这里填写支持条件 */;
//    }
//}
//
///*
// * 🎯 练习提示：
// * 
// * 1. 注入策略列表：@Autowired List<PromotionStrategy> allStrategies
// * 
// * 2. 过滤适用策略：strategy.supports(order) && strategy != this
// * 
// * 3. 单策略比较：result.getFinalAmount().compareTo(bestFinalAmount) < 0
// * 
// * 4. 支持条件示例：
// *    return allStrategies.stream()
// *           .filter(s -> s != this && s.supports(order))
// *           .count() > 1;
// * 
// * 💡 完成后思考：
// * - 新增策略是否需要修改这个类？
// * - 组合逻辑是否易于理解和测试？
// * - 这比巨大的if-else好在哪里？
// */