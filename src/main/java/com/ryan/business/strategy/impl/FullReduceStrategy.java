package com.ryan.business.strategy.impl;

import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import com.ryan.business.strategy.PromotionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ✨ 满减促销策略
 * 
 * 优点分析：
 * 1. 职责单一：只负责满减逻辑
 * 2. 易于测试：可以单独为这个类编写测试
 * 3. 易于扩展：新增满减规则只需修改这个类
 * 4. 代码清晰：逻辑简单明了
 */
@Component
public class FullReduceStrategy implements PromotionStrategy {
    
    @Override
    public PromotionResult calculate(PromotionOrder order) {
        BigDecimal originalAmount = order.getOriginalAmount();
        BigDecimal finalAmount = originalAmount;
        List<String> appliedPromotions = new ArrayList<>();
        StringBuilder description = new StringBuilder("满减促销：");
        
        // 满减规则
        if (originalAmount.compareTo(new BigDecimal("500")) >= 0) {
            finalAmount = finalAmount.subtract(new BigDecimal("100"));
            appliedPromotions.add("满500减100");
            description.append("满500减100；");
        } else if (originalAmount.compareTo(new BigDecimal("200")) >= 0) {
            finalAmount = finalAmount.subtract(new BigDecimal("50"));
            appliedPromotions.add("满200减50");
            description.append("满200减50；");
        } else if (originalAmount.compareTo(new BigDecimal("100")) >= 0) {
            finalAmount = finalAmount.subtract(new BigDecimal("20"));
            appliedPromotions.add("满100减20");
            description.append("满100减20；");
        } else {
            description.append("未达到满减门槛；");
        }
        
        // 会员等级加成
        finalAmount = applyMemberBonus(order, finalAmount, appliedPromotions, description);
        
        BigDecimal discountAmount = originalAmount.subtract(finalAmount);
        return new PromotionResult(originalAmount, finalAmount, discountAmount, 
                                 appliedPromotions, description.toString());
    }
    
    private BigDecimal applyMemberBonus(PromotionOrder order, BigDecimal amount, 
                                       List<String> appliedPromotions, StringBuilder description) {
        if ("VIP".equals(order.getUserLevel())) {
            BigDecimal discount = amount.multiply(new BigDecimal("0.05"));
            appliedPromotions.add("VIP额外5%折扣");
            description.append("VIP额外5%折扣；");
            return amount.subtract(discount);
        } else if ("SVIP".equals(order.getUserLevel())) {
            BigDecimal discount = amount.multiply(new BigDecimal("0.08"));
            appliedPromotions.add("SVIP额外8%折扣");
            description.append("SVIP额外8%折扣；");
            return amount.subtract(discount);
        }
        return amount;
    }
    
    @Override
    public String getStrategyName() {
        return "满减促销";
    }
    
    @Override
    public boolean supports(PromotionOrder order) {
        return order.getOriginalAmount().compareTo(new BigDecimal("100")) >= 0;
    }
}