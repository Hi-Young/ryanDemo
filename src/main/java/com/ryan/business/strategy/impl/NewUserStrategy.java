package com.ryan.business.strategy.impl;

import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import com.ryan.business.strategy.PromotionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ✨ 新用户专享策略
 */
@Component
public class NewUserStrategy implements PromotionStrategy {
    
    @Override
    public PromotionResult calculate(PromotionOrder order) {
        BigDecimal originalAmount = order.getOriginalAmount();
        BigDecimal finalAmount = originalAmount;
        List<String> appliedPromotions = new ArrayList<>();
        StringBuilder description = new StringBuilder("新用户专享：");
        
        // 首单立减50
        finalAmount = finalAmount.subtract(new BigDecimal("50"));
        appliedPromotions.add("新用户首单立减50");
        description.append("首单立减50；");
        
        // 满100再减20
        if (originalAmount.compareTo(new BigDecimal("100")) >= 0) {
            finalAmount = finalAmount.subtract(new BigDecimal("20"));
            appliedPromotions.add("新用户满100再减20");
            description.append("满100再减20；");
        }
        
        // 满300享受7.5折
        if (originalAmount.compareTo(new BigDecimal("300")) >= 0) {
            finalAmount = finalAmount.multiply(new BigDecimal("0.75"));
            appliedPromotions.add("新用户满300享7.5折");
            description.append("满300享7.5折；");
        }
        
        // 确保不为负数
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            finalAmount = new BigDecimal("0.01");
        }
        
        BigDecimal discountAmount = originalAmount.subtract(finalAmount);
        return new PromotionResult(originalAmount, finalAmount, discountAmount, 
                                 appliedPromotions, description.toString());
    }
    
    @Override
    public String getStrategyName() {
        return "新用户专享";
    }
    
    @Override
    public boolean supports(PromotionOrder order) {
        return order.isNewUser(); // 只支持新用户
    }
}