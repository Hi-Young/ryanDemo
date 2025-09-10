package com.ryan.business.entity;

import java.math.BigDecimal;
import java.util.List;

public class PromotionResult {
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private BigDecimal discountAmount;
    private List<String> appliedPromotions;
    private String description;
    
    public PromotionResult() {}
    
    public PromotionResult(BigDecimal originalAmount, BigDecimal finalAmount, 
                          BigDecimal discountAmount, List<String> appliedPromotions, 
                          String description) {
        this.originalAmount = originalAmount;
        this.finalAmount = finalAmount;
        this.discountAmount = discountAmount;
        this.appliedPromotions = appliedPromotions;
        this.description = description;
    }
    
    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }
    
    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }
    
    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public List<String> getAppliedPromotions() {
        return appliedPromotions;
    }
    
    public void setAppliedPromotions(List<String> appliedPromotions) {
        this.appliedPromotions = appliedPromotions;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}