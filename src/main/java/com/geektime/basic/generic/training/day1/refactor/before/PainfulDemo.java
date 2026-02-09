package com.geektime.basic.generic.training.day1.refactor.before;

import com.geektime.basic.generic.training.day1.entities.Product;
import com.geektime.basic.generic.training.day1.entities.User;

import java.math.BigDecimal;

/**
 * 演示：没有泛型时的痛苦
 *
 * 运行这个类，感受一下重复代码的痛苦
 */
public class PainfulDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   没有泛型时的代码重复问题");
        System.out.println("========================================\n");

        // 使用UserBox
        System.out.println("【场景1】存储用户");
        UserBox userBox = new UserBox();
        userBox.add(new User(1L, "张三", "zhang@example.com", 25));
        userBox.add(new User(2L, "李四", "li@example.com", 30));
        System.out.println(userBox);
        System.out.println("第一个用户: " + userBox.getFirst());

        // 使用ProductBox
        System.out.println("\n【场景2】存储商品");
        ProductBox productBox = new ProductBox();
        productBox.add(new Product("P-001", "iPhone", new BigDecimal("5999"), 100));
        productBox.add(new Product("P-002", "MacBook", new BigDecimal("12999"), 50));
        System.out.println(productBox);
        System.out.println("第一个商品: " + productBox.getFirst());

        // 使用OrderBox
        System.out.println("\n【场景3】存储订单");
        OrderBox orderBox = new OrderBox();
        orderBox.add(new Order("ORD-001", "张三"));
        orderBox.add(new Order("ORD-002", "李四"));
        System.out.println(orderBox);
        System.out.println("第一个订单: " + orderBox.getFirst());

        System.out.println("\n========================================");
        System.out.println("💔 痛点分析：");
        System.out.println("----------------------------------------");
        System.out.println("1. UserBox、ProductBox、OrderBox 代码90%相同");
        System.out.println("2. 如果要修改 add() 方法逻辑，需要改3个地方");
        System.out.println("3. 如果再增加一个实体，又要复制粘贴一遍");
        System.out.println("4. 总代码量：300行+（重复代码）");
        System.out.println("\n🎯 解决方案：用泛型！");
        System.out.println("   - 一个 GenericBox<T> 替代3个重复的类");
        System.out.println("   - 代码量减少到 50 行");
        System.out.println("   - 支持任意类型：User、Product、Order、...");
        System.out.println("========================================");

        GenericBox<User> userGenericBox = new GenericBox<>();
        userGenericBox.add(new User(1L, "张三", "zhang@example.com", 25));
        userGenericBox.add(new User(2L, "李四", "li@example.com", 30));
        System.out.println(userGenericBox);
        System.out.println("第一个用户: " + userGenericBox.getFirst());
    }
}
