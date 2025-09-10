package com.ryan.business.strategy;

import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;

/**
 * ✨ 策略模式：促销策略接口
 * 
 * 策略模式核心思想：
 * 1. 定义一系列算法（促销规则）
 * 2. 把它们一个个封装起来
 * 3. 并且使它们可相互替换
 */
public interface PromotionStrategy {
    
    /**
     * 计算促销结果
     * @param order 订单信息
     * @return 促销计算结果
     */
    PromotionResult calculate(PromotionOrder order);
    
    /**
     * 获取策略名称
     * @return 策略名称
     */
    String getStrategyName();
    
    /**
     * 判断是否支持该订单
     * @param order 订单信息
     * @return true：支持，false：不支持
     */
    boolean supports(PromotionOrder order);
}