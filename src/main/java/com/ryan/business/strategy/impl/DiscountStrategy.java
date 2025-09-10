package com.ryan.business.strategy.impl;

import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import com.ryan.business.strategy.PromotionStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ✨ 打折促销策略
 */
@Component
public class DiscountStrategy implements PromotionStrategy {
    
    @Override
    public PromotionResult calculate(PromotionOrder order) {
        BigDecimal originalAmount = order.getOriginalAmount();
        BigDecimal finalAmount = originalAmount;
        List<String> appliedPromotions = new ArrayList<>();
        StringBuilder description = new StringBuilder("打折促销：");
        
        // 分层打折
        if (originalAmount.compareTo(new BigDecimal("300")) >= 0) {
            finalAmount = finalAmount.multiply(new BigDecimal("0.8"));
            appliedPromotions.add("8折优惠");
            description.append("8折优惠；");
        } else if (originalAmount.compareTo(new BigDecimal("150")) >= 0) {
            finalAmount = finalAmount.multiply(new BigDecimal("0.85"));
            appliedPromotions.add("85折优惠");
            description.append("85折优惠；");
        } else {
            finalAmount = finalAmount.multiply(new BigDecimal("0.9"));
            appliedPromotions.add("9折优惠");
            description.append("9折优惠；");
        }
        
        // 新用户额外优惠
        if (order.isNewUser()) {
            BigDecimal newUserDiscount = new BigDecimal("50");
            if (finalAmount.compareTo(newUserDiscount) > 0) {
                finalAmount = finalAmount.subtract(newUserDiscount);
                appliedPromotions.add("新用户立减50");
                description.append("新用户立减50；");
            }
        }
        
        // 会员等级折扣
        finalAmount = applyMemberDiscount(order, finalAmount, appliedPromotions, description);
        
        BigDecimal discountAmount = originalAmount.subtract(finalAmount);
        return new PromotionResult(originalAmount, finalAmount, discountAmount, 
                                 appliedPromotions, description.toString());
    }
    
    private BigDecimal applyMemberDiscount(PromotionOrder order, BigDecimal amount, 
                                          List<String> appliedPromotions, StringBuilder description) {
        if ("VIP".equals(order.getUserLevel())) {
            amount = amount.multiply(new BigDecimal("0.95"));
            appliedPromotions.add("VIP会员9.5折");
            description.append("VIP会员9.5折；");
        } else if ("SVIP".equals(order.getUserLevel())) {
            amount = amount.multiply(new BigDecimal("0.92"));
            appliedPromotions.add("SVIP会员9.2折");
            description.append("SVIP会员9.2折；");
        }
        return amount;
    }
    
    @Override
    public String getStrategyName() {
        return "打折促销";
    }
    
    @Override
    public boolean supports(PromotionOrder order) {
        return true; // 打折促销支持所有订单
    }
}