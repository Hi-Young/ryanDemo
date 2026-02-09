package com.ryan.business.exercise;

import com.ryan.business.entity.OrderItem;
import com.ryan.business.entity.PromotionOrder;
import com.ryan.business.entity.PromotionResult;
import com.ryan.business.service.BadPromotionService;
import com.ryan.business.service.GoodPromotionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 🎯 策略模式演示程序
 * 
 * 运行这个Demo来直观感受两种实现的差别！
 * 
 * 运行方式：
 * 1. 在application.yml中设置 spring.profiles.active: demo
 * 2. 或启动时添加参数：java -jar app.jar --spring.profiles.active=demo
 */
@Component
@Profile("demo")
public class StrategyPatternDemo implements CommandLineRunner {
    
    private final BadPromotionService badService;
    private final GoodPromotionService goodService;
    
    public StrategyPatternDemo(BadPromotionService badService, GoodPromotionService goodService) {
        this.badService = badService;
        this.goodService = goodService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🎯 ===== 策略模式学习演示 =====\n");
        
        // 创建测试订单
        PromotionOrder testOrder = createTestOrder();
        
        printOrderInfo(testOrder);
        
        // 对比演示
        System.out.println("📊 ===== 两种实现方式对比 =====\n");
        
        // 1. 糟糕实现演示
        demonstrateBadImplementation(testOrder);
        
        System.out.println();
        
        // 2. 策略模式演示
        demonstrateGoodImplementation(testOrder);
        
        // 3. 扩展性演示
        System.out.println("\n🚀 ===== 扩展性演示 =====");
        demonstrateExtensibility();
        
        System.out.println("\n🎉 演示完成！现在去完成练习吧！");
    }
    
    private PromotionOrder createTestOrder() {
        return new PromotionOrder(
                "DEMO_ORDER_001",
                "USER_123", 
                Arrays.asList(
                        new OrderItem("P001", "iPhone 15", new BigDecimal("200"), 2, "电子"),
                        new OrderItem("P002", "AirPods", new BigDecimal("50"), 1, "配件")
                ),
                new BigDecimal("450"),
                true,  // 新用户
                "VIP", // VIP用户
                0
        );
    }
    
    private void printOrderInfo(PromotionOrder order) {
        System.out.println("📦 测试订单信息：");
        System.out.println("   订单ID: " + order.getOrderId());
        System.out.println("   用户ID: " + order.getUserId());
        System.out.println("   是否新用户: " + (order.isNewUser() ? "是" : "否"));
        System.out.println("   用户等级: " + order.getUserLevel());
        System.out.println("   订单金额: " + order.getOriginalAmount() + "元");
        System.out.println("   商品列表:");
        order.getItems().forEach(item -> 
            System.out.println("     - " + item.getProductName() + 
                             " (" + item.getCategory() + ") × " + item.getQuantity() + 
                             " = " + item.getSubTotal() + "元")
        );
        System.out.println();
    }
    
    private void demonstrateBadImplementation(PromotionOrder order) {
        System.out.println("🚫 糟糕实现（巨大的if-else方法）：");
        
        long startTime = System.nanoTime();
        PromotionResult fullReduceResult = badService.calculatePromotion(order, "FULL_REDUCE");
        PromotionResult discountResult = badService.calculatePromotion(order, "DISCOUNT");
        PromotionResult newUserResult = badService.calculatePromotion(order, "NEW_USER_SPECIAL");
        long endTime = System.nanoTime();
        
        System.out.println("   满减促销: " + fullReduceResult.getFinalAmount() + "元");
        System.out.println("   打折促销: " + discountResult.getFinalAmount() + "元");
        System.out.println("   新用户专享: " + newUserResult.getFinalAmount() + "元");
        System.out.println("   执行耗时: " + (endTime - startTime) / 1_000_000.0 + "ms");
        
        System.out.println("\n❌ 糟糕实现的问题：");
        System.out.println("   - 单个方法200+行代码，难以阅读");
        System.out.println("   - 新增促销需要修改核心方法，风险高");
        System.out.println("   - if-else嵌套复杂，容易出错");
        System.out.println("   - 无法单独测试某种促销逻辑");
        System.out.println("   - 代码重复度高，维护成本大");
    }
    
    private void demonstrateGoodImplementation(PromotionOrder order) {
        System.out.println("✅ 策略模式实现（优雅的面向对象设计）：");
        
        long startTime = System.nanoTime();
        PromotionResult fullReduceResult = goodService.calculateByStrategyName(order, "满减促销");
        PromotionResult discountResult = goodService.calculateByStrategyName(order, "打折促销");
        PromotionResult newUserResult = goodService.calculateByStrategyName(order, "新用户专享");
        PromotionResult bestResult = goodService.calculateBestStrategy(order);
        long endTime = System.nanoTime();
        
        System.out.println("   满减促销: " + fullReduceResult.getFinalAmount() + "元");
        System.out.println("   打折促销: " + discountResult.getFinalAmount() + "元");
        System.out.println("   新用户专享: " + newUserResult.getFinalAmount() + "元");
        System.out.println("   🎯 最优策略: " + bestResult.getFinalAmount() + "元");
        System.out.println("   执行耗时: " + (endTime - startTime) / 1_000_000.0 + "ms");
        
        System.out.println("\n✨ 策略模式的优势：");
        System.out.println("   - 每个策略独立成类，职责单一，易读易懂");
        System.out.println("   - 新增策略只需创建新类，无需修改现有代码");
        System.out.println("   - Spring自动管理策略实例，配置简单");
        System.out.println("   - 每个策略可独立测试，测试覆盖率高");
        System.out.println("   - 支持灵活组合，可以自动选择最优策略");
    }
    
    private void demonstrateExtensibility() {
        System.out.println("\n如果现在要新增一个'限时抢购'策略：");
        
        System.out.println("\n🚫 糟糕实现需要：");
        System.out.println("   1. 在巨大的if-else方法中添加新分支");
        System.out.println("   2. 可能影响现有的促销逻辑");
        System.out.println("   3. 需要重新测试所有促销类型");
        System.out.println("   4. 代码变得更加臃肿难读");
        
        System.out.println("\n✅ 策略模式只需要：");
        System.out.println("   1. 创建 FlashSaleStrategy 类");
        System.out.println("   2. 实现 PromotionStrategy 接口");
        System.out.println("   3. 添加 @Component 注解");
        System.out.println("   4. 完成！零风险，零影响！");
        
        System.out.println("\n🎯 这就是设计模式的威力：");
        System.out.println("   📈 让代码更容易扩展");
        System.out.println("   🛡️ 降低修改现有代码的风险");
        System.out.println("   🧪 提高代码的可测试性");
        System.out.println("   👥 提升团队协作效率");
    }
}

/*
 * 🎯 运行这个Demo的方法：
 * 
 * 1. 命令行运行：
 *    mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=demo
 * 
 * 2. IDE运行：
 *    在 application.yml 中添加：spring.profiles.active: demo
 * 
 * 3. 或者在启动类中直接调用这个方法
 * 
 * 🎉 看完演示后，赶紧去完成练习吧！
 */