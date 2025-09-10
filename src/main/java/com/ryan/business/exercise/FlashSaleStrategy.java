//package com.ryan.business.exercise;
//
//import com.ryan.business.entity.OrderItem;
//import com.ryan.business.entity.PromotionOrder;
//import com.ryan.business.entity.PromotionResult;
//import com.ryan.business.strategy.PromotionStrategy;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 🎯 练习2：限时抢购策略
// * 
// * 任务：实现限时抢购功能
// * 特点：时间限制、分类折扣、用户限制、库存检查
// */
//@Component
//public class FlashSaleStrategy implements PromotionStrategy {
//
//    // 模拟配置：抢购时间段
//    private final Map<String, String> flashSaleTimeConfig;
//
//    // 模拟配置：分类折扣
//    private final Map<String, BigDecimal> categoryDiscountConfig;
//
//    public FlashSaleStrategy() {
//        // 初始化抢购时间配置
//        flashSaleTimeConfig = new HashMap<>();
//        flashSaleTimeConfig.put("START_TIME", "10:00");
//        flashSaleTimeConfig.put("END_TIME", "12:00");
//
//        // 初始化分类折扣配置
//        categoryDiscountConfig = new HashMap<>();
//        categoryDiscountConfig.put("电子", new BigDecimal("0.5"));    // 电子产品5折
//        categoryDiscountConfig.put("配件", new BigDecimal("0.7"));    // 配件7折
//        categoryDiscountConfig.put("服装", new BigDecimal("0.6"));    // 服装6折
//    }
//
//    @Override
//    public PromotionResult calculate(PromotionOrder order) {
//        BigDecimal originalAmount = order.getOriginalAmount();
//        BigDecimal finalAmount = originalAmount;
//        List<String> appliedPromotions = new ArrayList<>();
//        StringBuilder description = new StringBuilder("限时抢购：");
//
//        // TODO: 1. 检查是否在抢购时间内
//        if (!isInFlashSaleTime()) {
//            return createFailResult(originalAmount, "不在抢购时间内");
//        }
//
//        // TODO: 2. 检查用户购买限制
//        if (!checkUserPurchaseLimit(order)) {
//            return createFailResult(originalAmount, "超出用户购买限制");
//        }
//
//        // TODO: 3. 检查库存
//        if (!checkInventory(order)) {
//            return createFailResult(originalAmount, "库存不足");
//        }
//
//        // TODO: 4. 按分类计算折扣
//        BigDecimal totalDiscount = BigDecimal.ZERO;
//
//        for (OrderItem item : order.getItems()) {
//            // TODO: 获取商品分类的折扣率
//            BigDecimal discountRate = categoryDiscountConfig.get(item.getCategory())/* 在这里获取分类折扣率 */;
//
//            if (discountRate != null) {
//                // TODO: 计算单个商品的折扣金额
//                BigDecimal itemSubTotal = item.getSubTotal();
//                BigDecimal itemDiscount = itemSubTotal.multiply(discountRate);
//                totalDiscount = totalDiscount.add(itemDiscount);
//
//                appliedPromotions.add(item.getCategory() + "分类" + 
//                    discountRate.multiply(BigDecimal.valueOf(10)).intValue() + "折");
//            }
//        }
//
//        finalAmount = finalAmount.subtract(totalDiscount);
//        description.append("总优惠").append(totalDiscount).append("元；");
//
//        // TODO: 5. VIP用户额外优惠
//        if (isVipUser(order)) {
//            BigDecimal vipExtraDiscount = finalAmount.multiply(new BigDecimal("0.05"));
//            finalAmount = finalAmount.subtract(vipExtraDiscount);
//            appliedPromotions.add("VIP额外5%优惠");
//            description.append("VIP额外5%优惠；");
//        }
//
//        BigDecimal discountAmount = originalAmount.subtract(finalAmount);
//        return new PromotionResult(originalAmount, finalAmount, discountAmount, 
//                                 appliedPromotions, description.toString());
//    }
//
//    // TODO: 实现时间检查
//    private boolean isInFlashSaleTime() {
//        LocalDateTime now = LocalDateTime.now();
//        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
//
//        String startTime = flashSaleTimeConfig.get("START_TIME");
//        String endTime = flashSaleTimeConfig.get("END_TIME");
//
//        // TODO: 比较当前时间是否在配置的时间段内
//        return currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0;
//    }
//
//    // TODO: 实现用户购买限制检查
//    private boolean checkUserPurchaseLimit(PromotionOrder order) {
//        // 模拟：每个用户限购2次
//        // TODO: 实际项目中应该查询数据库
//        // SELECT COUNT(*) FROM user_promotion_record 
//        // WHERE user_id = ? AND strategy_name = '限时抢购' AND DATE(created_time) = CURDATE()
//
//        int userTodayFlashSaleCount = getUserTodayFlashSaleCount(order.getUserId());
//        return userTodayFlashSaleCount < 2;
//    }
//
//    // TODO: 实现库存检查
//    private boolean checkInventory(PromotionOrder order) {
//        // 模拟库存检查
//        for (OrderItem item : order.getItems()) {
//            int availableStock = getAvailableStock(item.getProductId());
//
//            // TODO: 检查库存是否足够
//            if (availableStock > 0) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    // 模拟方法
//    private int getUserTodayFlashSaleCount(String userId) {
//        // TODO: 实际应该查询数据库
//        return 0; // 模拟返回0次
//    }
//
//    private int getAvailableStock(String productId) {
//        // TODO: 实际应该查询库存表
//        return 100; // 模拟库存充足
//    }
//
//    private boolean isVipUser(PromotionOrder order) {
//        return "VIP".equals(order.getUserLevel()) || "SVIP".equals(order.getUserLevel());
//    }
//
//    private PromotionResult createFailResult(BigDecimal originalAmount, String reason) {
//        List<String> reasons = new ArrayList<>();
//        reasons.add(reason);
//        return new PromotionResult(originalAmount, originalAmount, BigDecimal.ZERO,
//                                 reasons, "限时抢购失败：" + reason);
//    }
//
//    @Override
//    public String getStrategyName() {
//        return "限时抢购";
//    }
//
//    @Override
//    public boolean supports(PromotionOrder order) {
//        if (order == null) {
//            return false;
//        }
//        
//        // TODO: 支持条件
//        return /* 在这里填写支持条件，提示：需要在抢购时间内且有适用的商品分类 */;
//    }
//}
//
///*
// * 🎯 练习提示：
// * 
// * 1. 时间比较：currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0
// * 
// * 2. 获取分类折扣：categoryDiscountConfig.get(item.getCategory())
// * 
// * 3. 计算折扣：BigDecimal.ONE.subtract(discountRate)
// * 
// * 4. 购买限制：userTodayFlashSaleCount < 2
// * 
// * 5. 库存检查：availableStock < item.getQuantity()
// * 
// * 6. 支持条件：isInFlashSaleTime() && order.getItems().stream().anyMatch(item -> 
// *              categoryDiscountConfig.containsKey(item.getCategory()))
// * 
// * 💡 完成后思考：
// * - 如果要新增更多限制条件，代码如何变化？
// * - 这个策略是否易于测试？
// * - 相比在巨大方法中写这些逻辑，优势在哪里？
// */