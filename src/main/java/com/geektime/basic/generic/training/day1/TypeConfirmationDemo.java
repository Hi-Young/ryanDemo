package com.geektime.basic.generic.training.day1;

import com.geektime.basic.generic.training.day1.entities.Product;
import com.geektime.basic.generic.training.day1.entities.User;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 演示：子类继承时，泛型参数如何被具体化
 */
public class TypeConfirmationDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  演示：泛型参数的具体化");
        System.out.println("========================================\n");

        testUserRepository();
        testProductRepository();
        testCompileTimeTypeSafety();
    }

    private static void testUserRepository() {
        System.out.println("【UserRepository<User, Long>】");
        System.out.println("----------------------------------------");

        UserRepository userRepo = new UserRepository();
        User user = new User("张三", "zhang@example.com", 25);
        User saved = userRepo.save(user);

        // findById 的参数类型是 Long（不是泛型ID，而是具体的Long）
        Long userId = saved.getId();
        Optional<User> found = userRepo.findById(userId);
        //                                        ↑
        //                              这里必须传入Long类型
        //                              编译器知道 ID = Long

        System.out.println("✓ findById() 的参数类型：Long");
        System.out.println("✓ findById() 的返回类型：Optional<User>");
        System.out.println("✓ 找到的用户：" + found.orElse(null));

        // ❌ 如果传入错误的类型，编译器会报错
        // userRepo.findById("wrong-type");  // 编译错误！需要Long，不能传String
        // userRepo.findById(123);           // OK，123是int会自动装箱成Long

        System.out.println();
    }

    private static void testProductRepository() {
        System.out.println("【ProductRepository<Product, String>】");
        System.out.println("----------------------------------------");

        ProductRepository productRepo = new ProductRepository();
        Product product = new Product("iPhone", new BigDecimal("5999"), 100);
        Product saved = productRepo.save(product);

        // findById 的参数类型是 String（不是泛型ID，而是具体的String）
        String productCode = saved.getProductCode();
        Optional<Product> found = productRepo.findById(productCode);
        //                                              ↑
        //                                    这里必须传入String类型
        //                                    编译器知道 ID = String

        System.out.println("✓ findById() 的参数类型：String");
        System.out.println("✓ findById() 的返回类型：Optional<Product>");
        System.out.println("✓ 找到的商品：" + found.orElse(null));

        // ❌ 如果传入错误的类型，编译器会报错
        // productRepo.findById(123L);  // 编译错误！需要String，不能传Long

        System.out.println();
    }

    private static void testCompileTimeTypeSafety() {
        System.out.println("【编译时类型安全验证】");
        System.out.println("----------------------------------------");

        UserRepository userRepo = new UserRepository();
        ProductRepository productRepo = new ProductRepository();

        // ✅ 正确的用法
        userRepo.findById(1L);           // OK：参数类型是 Long
        productRepo.findById("P-001");   // OK：参数类型是 String

        // ❌ 下面这些会导致编译错误（取消注释试试）
        // userRepo.findById("wrong");   // ❌ 编译错误：需要Long，给了String
        // productRepo.findById(123L);   // ❌ 编译错误：需要String，给了Long

        System.out.println("✓ UserRepository.findById() 只接受 Long 类型");
        System.out.println("✓ ProductRepository.findById() 只接受 String 类型");
        System.out.println("✓ 编译器在编译时就能检查类型错误！");

        System.out.println("\n========================================");
        System.out.println("💡 关键理解：");
        System.out.println("----------------------------------------");
        System.out.println("1. 父类定义时：findById(ID id)  ← ID是泛型参数");
        System.out.println("2. UserRepository继承：ID = Long");
        System.out.println("   → findById(Long id) 被具体化");
        System.out.println("3. ProductRepository继承：ID = String");
        System.out.println("   → findById(String id) 被具体化");
        System.out.println("\n🎯 泛型让同一个方法支持不同的类型！");
        System.out.println("========================================");
    }
}
