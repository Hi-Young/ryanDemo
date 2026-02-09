package com.geektime.basic.generic.training.day5.before;

/**
 * 问题演示：链式调用时返回类型丢失
 *
 * 场景：Builder 模式的继承问题
 */
public class ChainCallProblem {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   链式调用的类型丢失问题");
        System.out.println("========================================\n");

        problem1_BuilderInheritance();
        problem2_EntityInheritance();

        System.out.println("\n========================================");
        System.out.println("💔 核心问题：");
        System.out.println("----------------------------------------");
        System.out.println("父类方法返回 this 时，类型是父类，而不是子类");
        System.out.println("导致链式调用无法继续调用子类的方法");
        System.out.println("\n🎯 解决方案：自限定类型 <T extends Base<T>>");
        System.out.println("========================================");
    }

    /**
     * 问题1：Builder 模式的继承问题
     */
    private static void problem1_BuilderInheritance() {
        System.out.println("【问题1】Builder 模式的继承问题");
        System.out.println("----------------------------------------");

        // ❌ 方式1：直接链式调用 - 编译错误！
//         UserBuilder user = new UserBuilder()
//             .setName("Tom")      // 返回 Builder，不是 UserBuilder
//             .setAge(18);         // ❌ Builder 没有 setAge 方法！

        // 😢 方式2：分步调用 - 失去了链式调用的优雅
        UserBuilder builder = new UserBuilder();
        builder.setName("Tom");  // 返回值被忽略
        builder.setAge(18);
        User user = builder.build();

        System.out.println("✗ 无法优雅地链式调用");
        System.out.println("✗ 必须分步调用，代码冗长");
        System.out.println();

        // 😢 方式3：强制类型转换 - 丑陋且不安全
        UserBuilder builder2 = (UserBuilder) new UserBuilder()
            .setName("Jerry");  // 需要强制转换
        builder2.setAge(20);

        System.out.println("✗ 需要手动强制类型转换");
        System.out.println("✗ 代码丑陋，容易出错");
        System.out.println();
    }

    /**
     * 问题2：实体类继承中的链式调用问题
     */
    private static void problem2_EntityInheritance() {
        System.out.println("【问题2】实体类继承中的链式调用");
        System.out.println("----------------------------------------");

        // ❌ 无法链式调用
//         Employee emp = new Employee()
//             .setId(1L)            // 返回 BaseEntity，不是 Employee
//             .setDepartment("IT"); // ❌ BaseEntity 没有 setDepartment 方法！

        // 😢 只能分步调用
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setName("Alice");
        emp.setDepartment("IT");

        System.out.println("✗ 实体类无法链式赋值");
        System.out.println("✗ Lombok 的 @Accessors(chain = true) 也有这个问题");
        System.out.println();
    }

    // ========================================
    // 问题代码示例
    // ========================================

    /**
     * ❌ 问题：父类 Builder 返回 this，类型是 Builder
     */
    static class Builder {
        protected String name;

        public Builder setName(String name) {
            this.name = name;
            return this;  // 💔 返回类型是 Builder，而不是子类
        }
    }

    /**
     * 子类 UserBuilder 想添加自己的方法
     */
    static class UserBuilder extends Builder {
        private int age;

        public UserBuilder setAge(int age) {
            this.age = age;
            return this;
        }

        public User build() {
            User user = new User();
            user.name = this.name;
            user.age = this.age;
            return user;
        }
    }

    static class User {
        String name;
        int age;
    }

    /**
     * ❌ 问题：实体类的链式调用
     */
    static class BaseEntity {
        protected Long id;
        protected String name;

        public BaseEntity setId(Long id) {
            this.id = id;
            return this;  // 💔 返回 BaseEntity，不是子类
        }

        public BaseEntity setName(String name) {
            this.name = name;
            return this;
        }
    }

    static class Employee extends BaseEntity {
        private String department;

        public Employee setDepartment(String department) {
            this.department = department;
            return this;
        }
    }
}
