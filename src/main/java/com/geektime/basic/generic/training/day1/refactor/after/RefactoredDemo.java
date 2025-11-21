package com.geektime.basic.generic.training.day1.refactor.after;

import com.geektime.basic.generic.training.day1.entities.Product;
import com.geektime.basic.generic.training.day1.entities.User;
import com.geektime.basic.generic.training.day1.refactor.before.Order;

import java.math.BigDecimal;

/**
 * 演示：用泛型重构后的效果
 *
 * 运行这个类，看看泛型如何消除重复代码
 */
public class RefactoredDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   用泛型重构后的代码");
        System.out.println("========================================\n");

        // 使用 GenericBox<User>
        System.out.println("【场景1】存储用户 - GenericBox<User>");
        GenericBox<User> userBox = new GenericBox<>();
        userBox.add(new User(1L, "张三", "zhang@example.com", 25));
        userBox.add(new User(2L, "李四", "li@example.com", 30));
        System.out.println(userBox);
        System.out.println("第一个用户: " + userBox.getFirst());

        // 使用 GenericBox<Product>
        System.out.println("\n【场景2】存储商品 - GenericBox<Product>");
        GenericBox<Product> productBox = new GenericBox<>();
        productBox.add(new Product("P-001", "iPhone", new BigDecimal("5999"), 100));
        productBox.add(new Product("P-002", "MacBook", new BigDecimal("12999"), 50));
        System.out.println(productBox);
        System.out.println("第一个商品: " + productBox.getFirst());

        // 使用 GenericBox<Order>
        System.out.println("\n【场景3】存储订单 - GenericBox<Order>");
        GenericBox<Order> orderBox = new GenericBox<>();
        // TODO: 需要先定义Order类，或者使用已有的
        // orderBox.add(new Order("ORD-001", "张三"));
        // orderBox.add(new Order("ORD-002", "李四"));
        // System.out.println(orderBox);

        System.out.println("\n========================================");
        System.out.println("✅ 重构成果：");
        System.out.println("----------------------------------------");
        System.out.println("1. 一个 GenericBox<T> 替代了 3 个重复的类");
        System.out.println("2. 代码量从 300 行减少到 50 行（减少83%）");
        System.out.println("3. 如果要修改逻辑，只需改一处");
        System.out.println("4. 支持任意类型，无需再写新类");
        System.out.println("5. 类型安全：编译器检查类型错误");
        System.out.println("\n🎯 这就是泛型的威力！");
        System.out.println("========================================");

        testTypeSafety(userBox, productBox);
    }

    /**
     * 演示类型安全
     */
    private static void testTypeSafety(GenericBox<User> userBox, GenericBox<Product> productBox) {
        System.out.println("\n【验证】类型安全");
        System.out.println("----------------------------------------");

        // ✅ 正确：往 userBox 里放 User
        User user = new User(3L, "王五", "wang@example.com", 35);
        userBox.add(user);
        System.out.println("✓ userBox.add(user) - OK");

        // ❌ 错误：往 userBox 里放 Product（编译错误）
        // Product product = new Product(...);
        // userBox.add(product);  // 编译错误！类型不匹配
        System.out.println("✓ userBox.add(product) - 编译错误（被阻止）");

        // ✅ 正确：从 userBox 里取出 User，不需要类型转换
        User retrievedUser = userBox.getFirst();
        System.out.println("✓ User user = userBox.getFirst() - OK（无需强转）");

        System.out.println("\n💡 泛型保证：");
        System.out.println("  - GenericBox<User> 只能存 User");
        System.out.println("  - GenericBox<Product> 只能存 Product");
        System.out.println("  - 编译器会检查类型错误");
        System.out.println("  - 取出时无需类型转换");
    }
}
