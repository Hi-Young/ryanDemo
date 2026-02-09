package com.ryan.business.controller;

import com.ryan.business.entity.OrderItem;
import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import com.ryan.business.service.BadPromotionService;
import com.ryan.business.service.GoodPromotionService;
import com.ryan.common.base.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion")
public class PromotionTestController {
    
    @Autowired
    private BadPromotionService badPromotionService;
    
    @Autowired
    private GoodPromotionService goodPromotionService;
    
    @GetMapping("/bad-example")
    public ResultVO<Map<String, PromotionResult>> testBadImplementation() {
        Map<String, PromotionResult> results = new HashMap<>();
        
        // 创建测试订单
        PromotionOrder order = createTestOrder();
        
        // 测试各种促销
        results.put("满减促销", badPromotionService.calculatePromotion(order, "FULL_REDUCE"));
        results.put("打折促销", badPromotionService.calculatePromotion(order, "DISCOUNT"));
        results.put("买赠促销", badPromotionService.calculatePromotion(order, "BUY_GET_FREE"));
        results.put("会员专享", badPromotionService.calculatePromotion(order, "MEMBER_EXCLUSIVE"));
        results.put("新用户专享", badPromotionService.calculatePromotion(order, "NEW_USER_SPECIAL"));
        results.put("节日特价", badPromotionService.calculatePromotion(order, "FESTIVAL_SPECIAL"));
        
        return ResultVO.success(results);
    }
    
    @PostMapping("/calculate")
    public ResultVO<PromotionResult> calculatePromotion(
            @RequestParam String promotionType,
            @RequestParam(defaultValue = "300") BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean isNewUser,
            @RequestParam(defaultValue = "NORMAL") String userLevel) {
        
        PromotionOrder order = new PromotionOrder(
                "ORDER_" + System.currentTimeMillis(),
                "USER_123",
                Arrays.asList(
                        new OrderItem("P001", "商品1", new BigDecimal("100"), 1, "电子"),
                        new OrderItem("P002", "商品2", new BigDecimal("200"), 1, "电子")
                ),
                amount,
                isNewUser,
                userLevel,
                0
        );
        
        PromotionResult result = badPromotionService.calculatePromotion(order, promotionType);
        return ResultVO.success(result);
    }
    
    private PromotionOrder createTestOrder() {
        return new PromotionOrder(
                "ORDER_TEST_001",
                "USER_123",
                Arrays.asList(
                        new OrderItem("P001", "iPhone 15", new BigDecimal("150"), 2, "电子"),
                        new OrderItem("P002", "AirPods", new BigDecimal("100"), 1, "电子"),
                        new OrderItem("P003", "保护壳", new BigDecimal("50"), 1, "配件")
                ),
                new BigDecimal("450"),
                true,  // 新用户
                "VIP", // VIP用户
                0
        );
    }

    // ===== ✨ 策略模式优雅实现 =====
    
    @GetMapping("/good-example")
    public ResultVO<Map<String, PromotionResult>> testGoodImplementation() {
        Map<String, PromotionResult> results = new HashMap<>();
        PromotionOrder order = createTestOrder();
        
        // 测试各种策略
        results.put("满减促销", goodPromotionService.calculateByStrategyName(order, "满减促销"));
        results.put("打折促销", goodPromotionService.calculateByStrategyName(order, "打折促销"));
        results.put("新用户专享", goodPromotionService.calculateByStrategyName(order, "新用户专享"));
        
        // 自动选择最优策略
        results.put("最优策略", goodPromotionService.calculateBestStrategy(order));
        
        return ResultVO.success(results);
    }
    
    @GetMapping("/compare/{strategyName}")
    public ResultVO<Map<String, Object>> compareImplementations(@PathVariable String strategyName) {
        PromotionOrder order = createTestOrder();
        
        // 策略模式映射
        Map<String, String> strategyMapping = new HashMap<>();
        strategyMapping.put("满减促销", "FULL_REDUCE");
        strategyMapping.put("打折促销", "DISCOUNT");
        strategyMapping.put("新用户专享", "NEW_USER_SPECIAL");
        
        Map<String, Object> comparison = new HashMap<>();
        
        // 糟糕实现
        String badType = strategyMapping.get(strategyName);
        if (badType != null) {
            comparison.put("糟糕实现", badPromotionService.calculatePromotion(order, badType));
        }
        
        // 策略模式实现
        comparison.put("策略模式", goodPromotionService.calculateByStrategyName(order, strategyName));
        
        // 添加对比说明
        Map<String, List<String>> compareInfo = new HashMap<>();
        compareInfo.put("糟糕实现问题", Arrays.asList(
                "单一方法200+行代码",
                "新增促销需要修改核心类",
                "if-else嵌套复杂，易出错",
                "无法单独测试某种促销逻辑",
                "代码重复，维护成本高"
        ));
        compareInfo.put("策略模式优势", Arrays.asList(
                "每个策略独立类，职责单一",
                "新增策略只需创建新类",
                "Spring自动管理策略，零配置",
                "可独立测试，可灵活组合",
                "遵循开闭原则，扩展性强"
        ));
        comparison.put("对比说明", compareInfo);
        
        return ResultVO.success(comparison);
    }
    
    @GetMapping("/strategies")
    public ResultVO<List<String>> getAvailableStrategies() {
        return ResultVO.success(goodPromotionService.getAvailableStrategies());
    }
    
    @GetMapping("/best-strategy")
    public ResultVO<PromotionResult> getBestStrategy(
            @RequestParam(defaultValue = "300") BigDecimal amount,
            @RequestParam(defaultValue = "true") boolean isNewUser,
            @RequestParam(defaultValue = "VIP") String userLevel) {
        
        PromotionOrder order = createCustomOrder(amount, isNewUser, userLevel);
        return ResultVO.success(goodPromotionService.calculateBestStrategy(order));
    }
    
    @GetMapping("/all-applicable")
    public ResultVO<List<PromotionResult>> getAllApplicable(
            @RequestParam(defaultValue = "300") BigDecimal amount,
            @RequestParam(defaultValue = "true") boolean isNewUser,
            @RequestParam(defaultValue = "VIP") String userLevel) {
        
        PromotionOrder order = createCustomOrder(amount, isNewUser, userLevel);
        return ResultVO.success(goodPromotionService.getAllApplicableStrategies(order));
    }
    
    private PromotionOrder createCustomOrder(BigDecimal amount, boolean isNewUser, String userLevel) {
        return new PromotionOrder(
                "ORDER_" + System.currentTimeMillis(),
                "USER_123",
                Arrays.asList(
                        new OrderItem("P001", "商品1", amount.divide(BigDecimal.valueOf(2)), 1, "电子"),
                        new OrderItem("P002", "商品2", amount.divide(BigDecimal.valueOf(2)), 1, "电子")
                ),
                amount,
                isNewUser,
                userLevel,
                0
        );
    }
}

/*
 * ✨ 策略模式 vs 传统实现对比总结：
 * 
 * 【扩展性】
 * 糟糕实现：修改200+行方法，高风险
 * 策略模式：新增一个类，零风险
 * 
 * 【可读性】
 * 糟糕实现：巨大的if-else嵌套
 * 策略模式：每个策略逻辑清晰
 * 
 * 【测试性】
 * 糟糕实现：无法单独测试
 * 策略模式：每个策略独立测试
 * 
 * 【维护性】
 * 糟糕实现：牵一发而动全身
 * 策略模式：修改某个策略不影响其他
 * 
 * 💡 这就是设计模式的威力！
 */