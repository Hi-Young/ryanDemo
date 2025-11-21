package com.geektime.basic.generic.training.day1;

import com.geektime.basic.generic.training.day1.entities.Product;
import com.geektime.basic.generic.training.day1.entities.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Day 1 测试类
 *
 * 运行这个类来验证你的实现是否正确
 *
 * 运行方式：右键 -> Run 'Day1Test.main()'
 */
public class Day1Test {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   Day 1: 泛型基础 + 通用DAO层 测试");
        System.out.println("========================================\n");

        testUserRepository();
        testProductRepository();

        System.out.println("\n========================================");
        System.out.println("🎉 恭喜！所有测试通过！");
        System.out.println("========================================");
        System.out.println("\n💡 反思时间：");
        System.out.println("1. 如果没有泛型，UserRepository和ProductRepository会有多少重复代码？");
        System.out.println("2. 泛型如何让你的代码做到一次编写，处处复用？");
        System.out.println("3. BaseRepository<T, ID> 中，为什么需要两个类型参数？");
        System.out.println("\n✅ 理解了这些问题，你就掌握了泛型的第一层威力！");
        System.out.println("\n🚀 准备好进入 Day 2 了吗？");

        UserRepository userRepo = new UserRepository();
        ProductRepository productRepo = new ProductRepository();

        User user = new User("张三", "zhang@example.com", 25);
        Product product = new Product("iPhone", new BigDecimal("5999"), 100);

        userRepo.save(user);       // ✅ 正确
        productRepo.save(product);
//        productRepo.save(user);
        
    }

    private static void testUserRepository() {
        System.out.println("【测试1】UserRepository<User, Long>");
        System.out.println("----------------------------------------");

        UserRepository userRepo = new UserRepository();

        // 测试保存
        User user1 = new User("张三", "zhangsan@example.com", 25);
        User user2 = new User("李四", "lisi@example.com", 30);

        User savedUser1 = userRepo.save(user1);
        User savedUser2 = userRepo.save(user2);

        System.out.println("✓ 保存用户成功: " + savedUser1);
        System.out.println("✓ 保存用户成功: " + savedUser2);

        assert savedUser1.getId() != null : "❌ 保存后ID应该被自动设置";
        assert savedUser2.getId() != null : "❌ 保存后ID应该被自动设置";

        // 测试查找
        Optional<User> found = userRepo.findById(savedUser1.getId());
        System.out.println("✓ 根据ID查找用户: " + found.orElse(null));
        assert found.isPresent() : "❌ 应该能找到已保存的用户";
        assert found.get().getUsername().equals("张三") : "❌ 用户名应该是'张三'";

        // 测试查找所有
        List<User> allUsers = userRepo.findAll();
        System.out.println("✓ 查找所有用户数量: " + allUsers.size());
        assert allUsers.size() == 2 : "❌ 应该有2个用户";

        // 测试统计
        long count = userRepo.count();
        System.out.println("✓ 用户总数: " + count);
        assert count == 2 : "❌ 用户总数应该是2";

        // 测试更新
        savedUser1.setAge(26);
        User updated = userRepo.update(savedUser1);
        System.out.println("✓ 更新用户年龄: " + updated.getAge());
        assert updated.getAge() == 26 : "❌ 年龄应该被更新为26";

        // 测试删除
        boolean deleted = userRepo.deleteById(savedUser2.getId());
        System.out.println("✓ 删除用户成功: " + deleted);
        assert deleted : "❌ 删除应该成功";
        assert userRepo.count() == 1 : "❌ 删除后应该只剩1个用户";

        // 测试自定义方法
        User foundByUsername = userRepo.findByUsername("张三");
        System.out.println("✓ 根据用户名查找: " + foundByUsername);
        assert foundByUsername != null : "❌ 应该能根据用户名找到用户";

        System.out.println("\n✅ UserRepository 所有测试通过！\n");
    }

    private static void testProductRepository() {
        System.out.println("【测试2】ProductRepository<Product, String>");
        System.out.println("----------------------------------------");

        ProductRepository productRepo = new ProductRepository();

        // 测试保存
        Product product1 = new Product("iPhone 15", new BigDecimal("5999.00"), 100);
        Product product2 = new Product("MacBook Pro", new BigDecimal("12999.00"), 50);

        Product saved1 = productRepo.save(product1);
        Product saved2 = productRepo.save(product2);

        System.out.println("✓ 保存商品成功: " + saved1);
        System.out.println("✓ 保存商品成功: " + saved2);

        assert saved1.getProductCode() != null : "❌ 保存后商品编码应该被自动设置";
        assert saved2.getProductCode() != null : "❌ 保存后商品编码应该被自动设置";
        assert saved1.getProductCode().startsWith("P-") : "❌ 商品编码应该以P-开头";

        // 测试查找
        Optional<Product> found = productRepo.findById(saved1.getProductCode());
        System.out.println("✓ 根据编码查找商品: " + found.orElse(null));
        assert found.isPresent() : "❌ 应该能找到已保存的商品";

        // 测试查找所有
        List<Product> allProducts = productRepo.findAll();
        System.out.println("✓ 查找所有商品数量: " + allProducts.size());
        assert allProducts.size() == 2 : "❌ 应该有2个商品";

        // 测试自定义方法
        Product foundByName = productRepo.findByName("iPhone 15");
        System.out.println("✓ 根据商品名查找: " + foundByName);
        assert foundByName != null : "❌ 应该能根据商品名找到商品";

        System.out.println("\n✅ ProductRepository 所有测试通过！\n");

        // 💡 关键对比
        System.out.println("💡 关键发现：");
        System.out.println("  - UserRepository 使用 <User, Long>");
        System.out.println("  - ProductRepository 使用 <Product, String>");
        System.out.println("  - 但它们都继承自同一个 MemoryRepository<T, ID>");
        System.out.println("  - 这就是泛型的威力：一套代码，支持不同类型！");
    }
}
