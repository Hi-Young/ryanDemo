package com.ryan.business.service;

import com.ryan.business.entity.OrderItem;
import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🚫 糟糕的实现 - 巨大的促销服务类
 * 
 * 问题分析：
 * 1. 单一方法超过200行代码
 * 2. 大量if-else嵌套，逻辑复杂
 * 3. 职责不单一：包含所有促销逻辑
 * 4. 违反开闭原则：每次新增促销都要修改这个类
 * 5. 难以测试：无法单独测试某种促销逻辑
 * 6. 难以扩展：新增促销类型成本极高
 * 7. 代码重复：相似逻辑在不同分支中重复出现
 */
@Service
public class BadPromotionService {
    
    public PromotionResult calculatePromotion(PromotionOrder order, String promotionType) {
        BigDecimal originalAmount = order.getOriginalAmount();
        BigDecimal finalAmount = originalAmount;
        List<String> appliedPromotions = new ArrayList<>();
        StringBuilder description = new StringBuilder("促销详情：");
        
        // 🔥 巨大的if-else逻辑开始！
        if ("FULL_REDUCE".equals(promotionType)) {
            // 满减促销
            if (originalAmount.compareTo(new BigDecimal("100")) >= 0) {
                if (originalAmount.compareTo(new BigDecimal("500")) >= 0) {
                    finalAmount = finalAmount.subtract(new BigDecimal("100"));
                    appliedPromotions.add("满500减100");
                    description.append("满500减100；");
                } else if (originalAmount.compareTo(new BigDecimal("200")) >= 0) {
                    finalAmount = finalAmount.subtract(new BigDecimal("50"));
                    appliedPromotions.add("满200减50");
                    description.append("满200减50；");
                } else {
                    finalAmount = finalAmount.subtract(new BigDecimal("20"));
                    appliedPromotions.add("满100减20");
                    description.append("满100减20；");
                }
                
                // 🔥 还要考虑会员等级加成
                if ("VIP".equals(order.getUserLevel())) {
                    BigDecimal vipDiscount = finalAmount.multiply(new BigDecimal("0.05"));
                    finalAmount = finalAmount.subtract(vipDiscount);
                    appliedPromotions.add("VIP额外5%折扣");
                    description.append("VIP额外5%折扣；");
                } else if ("SVIP".equals(order.getUserLevel())) {
                    BigDecimal svipDiscount = finalAmount.multiply(new BigDecimal("0.08"));
                    finalAmount = finalAmount.subtract(svipDiscount);
                    appliedPromotions.add("SVIP额外8%折扣");
                    description.append("SVIP额外8%折扣；");
                }
            }
            
        } else if ("DISCOUNT".equals(promotionType)) {
            // 打折促销
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
            
            // 🔥 新用户还有额外优惠
            if (order.isNewUser()) {
                BigDecimal newUserDiscount = new BigDecimal("50");
                if (finalAmount.compareTo(newUserDiscount) > 0) {
                    finalAmount = finalAmount.subtract(newUserDiscount);
                    appliedPromotions.add("新用户立减50");
                    description.append("新用户立减50；");
                }
            }
            
            // 🔥 会员等级折扣
            if ("VIP".equals(order.getUserLevel())) {
                finalAmount = finalAmount.multiply(new BigDecimal("0.95"));
                appliedPromotions.add("VIP会员9.5折");
                description.append("VIP会员9.5折；");
            } else if ("SVIP".equals(order.getUserLevel())) {
                finalAmount = finalAmount.multiply(new BigDecimal("0.92"));
                appliedPromotions.add("SVIP会员9.2折");
                description.append("SVIP会员9.2折；");
            }
            
        } else if ("BUY_GET_FREE".equals(promotionType)) {
            // 买赠促销 - 买2送1
            Map<String, List<OrderItem>> categoryMap = order.getItems().stream()
                    .collect(Collectors.groupingBy(OrderItem::getCategory));
            
            BigDecimal totalDiscount = BigDecimal.ZERO;
            for (Map.Entry<String, List<OrderItem>> entry : categoryMap.entrySet()) {
                List<OrderItem> items = entry.getValue();
                if (items.size() >= 2) {
                    // 找到最便宜的商品免费
                    OrderItem cheapestItem = items.stream()
                            .min((item1, item2) -> item1.getPrice().compareTo(item2.getPrice()))
                            .orElse(null);
                    if (cheapestItem != null) {
                        int freeQuantity = items.stream().mapToInt(OrderItem::getQuantity).sum() / 3;
                        BigDecimal itemDiscount = cheapestItem.getPrice().multiply(BigDecimal.valueOf(freeQuantity));
                        totalDiscount = totalDiscount.add(itemDiscount);
                        appliedPromotions.add("买2送1优惠（" + entry.getKey() + "类别）");
                        description.append("买2送1优惠（").append(entry.getKey()).append("类别）；");
                    }
                }
            }
            finalAmount = finalAmount.subtract(totalDiscount);
            
            // 🔥 还要考虑节日加成
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
            if ("11-11".equals(today) || "12-12".equals(today)) {
                BigDecimal festivalDiscount = finalAmount.multiply(new BigDecimal("0.1"));
                finalAmount = finalAmount.subtract(festivalDiscount);
                appliedPromotions.add("双11/双12特价10%折扣");
                description.append("双11/双12特价10%折扣；");
            } else if ("06-18".equals(today)) {
                BigDecimal festivalDiscount = finalAmount.multiply(new BigDecimal("0.08"));
                finalAmount = finalAmount.subtract(festivalDiscount);
                appliedPromotions.add("618特价8%折扣");
                description.append("618特价8%折扣；");
            }
            
        } else if ("MEMBER_EXCLUSIVE".equals(promotionType)) {
            // 会员专享促销
            if ("VIP".equals(order.getUserLevel()) || "SVIP".equals(order.getUserLevel())) {
                if ("SVIP".equals(order.getUserLevel())) {
                    finalAmount = finalAmount.multiply(new BigDecimal("0.85"));
                    appliedPromotions.add("SVIP专享8.5折");
                    description.append("SVIP专享8.5折；");
                    
                    // SVIP还有购买满额赠送积分
                    if (originalAmount.compareTo(new BigDecimal("1000")) >= 0) {
                        appliedPromotions.add("满1000赠送1000积分");
                        description.append("满1000赠送1000积分；");
                    }
                } else {
                    finalAmount = finalAmount.multiply(new BigDecimal("0.9"));
                    appliedPromotions.add("VIP专享9折");
                    description.append("VIP专享9折；");
                    
                    // VIP满额赠送积分
                    if (originalAmount.compareTo(new BigDecimal("500")) >= 0) {
                        appliedPromotions.add("满500赠送500积分");
                        description.append("满500赠送500积分；");
                    }
                }
                
                // 会员专享满减
                if (originalAmount.compareTo(new BigDecimal("300")) >= 0) {
                    finalAmount = finalAmount.subtract(new BigDecimal("30"));
                    appliedPromotions.add("会员专享满300减30");
                    description.append("会员专享满300减30；");
                }
                
            } else {
                // 非会员无法享受此促销
                appliedPromotions.add("需要VIP以上会员才能享受此促销");
                description.append("需要VIP以上会员才能享受此促销；");
                return new PromotionResult(originalAmount, originalAmount, BigDecimal.ZERO, 
                                         appliedPromotions, description.toString());
            }
            
        } else if ("NEW_USER_SPECIAL".equals(promotionType)) {
            // 新用户专享
            if (order.isNewUser()) {
                // 首单立减50
                finalAmount = finalAmount.subtract(new BigDecimal("50"));
                appliedPromotions.add("新用户首单立减50");
                description.append("新用户首单立减50；");
                
                // 满100再减20
                if (originalAmount.compareTo(new BigDecimal("100")) >= 0) {
                    finalAmount = finalAmount.subtract(new BigDecimal("20"));
                    appliedPromotions.add("新用户满100再减20");
                    description.append("新用户满100再减20；");
                }
                
                // 满300享受7.5折
                if (originalAmount.compareTo(new BigDecimal("300")) >= 0) {
                    BigDecimal currentAmount = finalAmount;
                    BigDecimal discountAmount = currentAmount.multiply(new BigDecimal("0.75"));
                    finalAmount = discountAmount;
                    appliedPromotions.add("新用户满300享7.5折");
                    description.append("新用户满300享7.5折；");
                }
            } else {
                appliedPromotions.add("仅新用户可享受此促销");
                description.append("仅新用户可享受此促销；");
                return new PromotionResult(originalAmount, originalAmount, BigDecimal.ZERO, 
                                         appliedPromotions, description.toString());
            }
            
        } else if ("FESTIVAL_SPECIAL".equals(promotionType)) {
            // 节日特价
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
            if ("11-11".equals(today)) {
                // 双11狂欢
                finalAmount = finalAmount.multiply(new BigDecimal("0.6"));
                appliedPromotions.add("双11狂欢4折");
                description.append("双11狂欢4折；");
                
                // 满500还能再减100
                if (originalAmount.compareTo(new BigDecimal("500")) >= 0) {
                    finalAmount = finalAmount.subtract(new BigDecimal("100"));
                    appliedPromotions.add("双11满500再减100");
                    description.append("双11满500再减100；");
                }
                
                // VIP和SVIP还有额外优惠
                if ("VIP".equals(order.getUserLevel())) {
                    finalAmount = finalAmount.multiply(new BigDecimal("0.95"));
                    appliedPromotions.add("VIP双11额外5%折扣");
                    description.append("VIP双11额外5%折扣；");
                } else if ("SVIP".equals(order.getUserLevel())) {
                    finalAmount = finalAmount.multiply(new BigDecimal("0.9"));
                    appliedPromotions.add("SVIP双11额外10%折扣");
                    description.append("SVIP双11额外10%折扣；");
                }
                
            } else if ("12-12".equals(today)) {
                // 双12特价
                finalAmount = finalAmount.multiply(new BigDecimal("0.7"));
                appliedPromotions.add("双12特价7折");
                description.append("双12特价7折；");
                
            } else if ("06-18".equals(today)) {
                // 618购物节
                finalAmount = finalAmount.multiply(new BigDecimal("0.75"));
                appliedPromotions.add("618购物节7.5折");
                description.append("618购物节7.5折；");
                
            } else {
                appliedPromotions.add("当前不在节日促销期间");
                description.append("当前不在节日促销期间；");
                return new PromotionResult(originalAmount, originalAmount, BigDecimal.ZERO, 
                                         appliedPromotions, description.toString());
            }
        } else {
            // 未知促销类型
            appliedPromotions.add("未知的促销类型：" + promotionType);
            description.append("未知的促销类型：").append(promotionType);
            return new PromotionResult(originalAmount, originalAmount, BigDecimal.ZERO, 
                                     appliedPromotions, description.toString());
        }
        
        // 🔥 最终价格不能为负数
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = new BigDecimal("0.01");
        }
        
        BigDecimal discountAmount = originalAmount.subtract(finalAmount);
        return new PromotionResult(originalAmount, finalAmount, discountAmount, 
                                 appliedPromotions, description.toString());
    }
}

/*
 * 🚫 这个类的问题总结：
 * 
 * 1. 【可读性差】单个方法200+行，逻辑复杂难懂
 * 2. 【维护成本高】每次新增促销都要修改这个巨大的方法
 * 3. 【测试困难】无法单独测试某个促销逻辑
 * 4. 【代码重复】会员等级判断逻辑重复出现
 * 5. 【违反单一职责】一个类承担了所有促销计算责任
 * 6. 【违反开闭原则】对修改开放，对扩展不友好
 * 7. 【耦合度高】促销逻辑和业务逻辑耦合在一起
 * 8. 【容易出错】复杂的嵌套if-else容易产生逻辑错误
 * 
 * 💡 接下来我们用策略模式重构，你会看到巨大的改善！
 */