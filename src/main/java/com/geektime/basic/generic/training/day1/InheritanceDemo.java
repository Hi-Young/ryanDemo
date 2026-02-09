package com.geektime.basic.generic.training.day1;

import com.geektime.basic.generic.training.day1.entities.Product;
import com.geektime.basic.generic.training.day1.entities.User;

import java.math.BigDecimal;

/**
 * 演示：子类如何继承父类的泛型方法
 */
public class InheritanceDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  演示：泛型方法的继承和复用");
        System.out.println("========================================\n");

        // 创建UserRepository
        UserRepository userRepo = new UserRepository();

        System.out.println("【UserRepository 继承的方法】");
        System.out.println("----------------------------------------");

        // ✅ save() 方法：继承自父类 MemoryRepository
        User user = new User("张三", "zhang@example.com", 25);
        User savedUser = userRepo.save(user);
        System.out.println("✓ save(user) 方法来自父类：" + savedUser);

        // ✅ findById() 方法：继承自父类
        userRepo.findById(savedUser.getId()).ifPresent(found -> {
            System.out.println("✓ findById(id) 方法来自父类：" + found);
        });

        // ✅ count() 方法：继承自父类
        long count = userRepo.count();
        System.out.println("✓ count() 方法来自父类：" + count);

        // ✅ findAll() 方法：继承自父类
        System.out.println("✓ findAll() 方法来自父类：" + userRepo.findAll().size() + " 条记录");

        System.out.println("\n【ProductRepository 继承的方法】");
        System.out.println("----------------------------------------");

        // 创建ProductRepository
        ProductRepository productRepo = new ProductRepository();

        // ✅ 同样的方法，Product也能用！
        Product product = new Product("iPhone", new BigDecimal("5999"), 100);
        Product savedProduct = productRepo.save(product);
        System.out.println("✓ save(product) 方法也来自父类：" + savedProduct);

        productRepo.findById(savedProduct.getProductCode()).ifPresent(found -> {
            System.out.println("✓ findById(code) 方法也来自父类：" + found);
        });

        System.out.println("✓ count() 方法也来自父类：" + productRepo.count());

        System.out.println("\n========================================");
        System.out.println("💡 关键发现：");
        System.out.println("----------------------------------------");
        System.out.println("1. save、findById、count等方法，父类只写了一次");
        System.out.println("2. UserRepository 和 ProductRepository 都能直接使用");
        System.out.println("3. 虽然一个处理User（Long主键），一个处理Product（String主键）");
        System.out.println("4. 但泛型让它们共享同一套逻辑！");
        System.out.println("\n🎯 这就是泛型的威力：");
        System.out.println("   写一次代码，到处复用！");
        System.out.println("========================================");
    }
}
