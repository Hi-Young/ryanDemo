package com.ryan.business.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PromotionOrder {
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private BigDecimal originalAmount;
    private boolean isNewUser;
    private String userLevel; // NORMAL, VIP, SVIP
    private int previousOrderCount;
    
    public PromotionOrder() {}
    
    public PromotionOrder(String orderId, String userId, List<OrderItem> items, 
                         BigDecimal originalAmount, boolean isNewUser, 
                         String userLevel, int previousOrderCount) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.originalAmount = originalAmount;
        this.isNewUser = isNewUser;
        this.userLevel = userLevel;
        this.previousOrderCount = previousOrderCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PromotionOrder that = (PromotionOrder) o;
        return isNewUser == that.isNewUser && previousOrderCount == that.previousOrderCount && Objects.equals(orderId, that.orderId) && Objects.equals(userId, that.userId) && Objects.equals(items, that.items) && Objects.equals(originalAmount, that.originalAmount) && Objects.equals(userLevel, that.userLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userId, items, originalAmount, isNewUser, userLevel, previousOrderCount);
    }

    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }
    
    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }
    
    public boolean isNewUser() {
        return isNewUser;
    }
    
    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }
    
    public String getUserLevel() {
        return userLevel;
    }
    
    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }
    
    public int getPreviousOrderCount() {
        return previousOrderCount;
    }
    
    public void setPreviousOrderCount(int previousOrderCount) {
        this.previousOrderCount = previousOrderCount;
    }
}