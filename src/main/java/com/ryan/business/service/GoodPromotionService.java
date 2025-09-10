package com.ryan.business.service;

import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import com.ryan.business.strategy.PromotionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✨ 策略模式优雅实现 - 促销服务
 * 
 * 优点分析：
 * 1. 职责单一：只负责策略的选择和协调
 * 2. 开闭原则：新增策略无需修改此类
 * 3. 易于扩展：Spring自动注入所有策略
 * 4. 灵活组合：支持多种策略选择方式
 * 5. 易于测试：可以轻松mock任何策略
 */
@Service
public class GoodPromotionService {
    
    private final Map<String, PromotionStrategy> strategyMap;
    
    @Autowired
    public GoodPromotionService(List<PromotionStrategy> strategies) {
        // Spring自动注入所有PromotionStrategy实现类
        // 将策略按名称映射，便于查找
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                    PromotionStrategy::getStrategyName, 
                    strategy -> strategy
                ));
    }
    
    /**
     * 根据策略名称计算促销
     */
    public PromotionResult calculateByStrategyName(PromotionOrder order, String strategyName) {
        PromotionStrategy strategy = strategyMap.get(strategyName);
        
        if (strategy == null) {
            return createErrorResult(order.getOriginalAmount(), "未找到促销策略：" + strategyName);
        }
        
        if (!strategy.supports(order)) {
            return createErrorResult(order.getOriginalAmount(), 
                    strategy.getStrategyName() + "不支持当前订单");
        }
        
        return strategy.calculate(order);
    }
    
    /**
     * 自动选择最优策略
     */
    public PromotionResult calculateBestStrategy(PromotionOrder order) {
        PromotionResult bestResult = null;
        BigDecimal lowestPrice = order.getOriginalAmount();
        String bestStrategyName = "无促销";
        
        for (PromotionStrategy strategy : strategyMap.values()) {
            if (strategy.supports(order)) {
                PromotionResult result = strategy.calculate(order);
                if (result.getFinalAmount().compareTo(lowestPrice) < 0) {
                    lowestPrice = result.getFinalAmount();
                    bestResult = result;
                    bestStrategyName = strategy.getStrategyName();
                }
            }
        }
        
        if (bestResult == null) {
            return createErrorResult(order.getOriginalAmount(), "没有适用的促销策略");
        }
        
        bestResult.setDescription(bestResult.getDescription() + "（最优策略：" + bestStrategyName + "）");
        return bestResult;
    }
    
    /**
     * 获取所有适用的策略
     */
    public List<PromotionResult> getAllApplicableStrategies(PromotionOrder order) {
        return strategyMap.values().stream()
                .filter(strategy -> strategy.supports(order))
                .map(strategy -> strategy.calculate(order))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有可用策略名称
     */
    public List<String> getAvailableStrategies() {
        return strategyMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    private PromotionResult createErrorResult(BigDecimal originalAmount, String errorMessage) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        return new PromotionResult(
                originalAmount, 
                originalAmount, 
                BigDecimal.ZERO,
                errors,
                errorMessage
        );
    }
}

/*
 * ✨ 策略模式的强大优势：
 * 
 * 1. 【扩展性】新增促销策略步骤：
 *    ① 创建新的Strategy实现类
 *    ② 加上@Component注解
 *    ③ 完成！无需修改任何现有代码
 * 
 * 2. 【灵活性】支持多种选择方式：
 *    - 按名称选择特定策略
 *    - 自动选择最优策略
 *    - 获取所有适用策略进行比较
 * 
 * 3. 【可测试性】每个策略可独立测试：
 *    - 单元测试更加简单
 *    - 集成测试更加稳定
 *    - 可以轻松模拟各种场景
 * 
 * 4. 【维护性】职责清晰：
 *    - Service只负责策略选择
 *    - Strategy只负责计算逻辑
 *    - 修改某个促销不影响其他促销
 */