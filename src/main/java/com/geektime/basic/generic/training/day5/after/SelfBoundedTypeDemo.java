package com.geektime.basic.generic.training.day5.after;

/**
 * 解决方案：自限定类型（Self-Bounded Type / F-Bounded Polymorphism）
 *
 * 核心思想：让类型参数约束自己
 * 语法：class Base<T extends Base<T>>
 */
public class SelfBoundedTypeDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   自限定类型解决链式调用问题");
        System.out.println("========================================\n");

        solution1_BuilderPattern();
        solution2_EntityInheritance();
        solution3_RealWorldExample();

        System.out.println("\n========================================");
        System.out.println("✅ 自限定类型的威力：");
        System.out.println("----------------------------------------");
        System.out.println("1. 链式调用返回正确的子类类型");
        System.out.println("2. 不需要强制类型转换");
        System.out.println("3. 编译时类型安全");
        System.out.println("========================================");
    }

    /**
     * 解决方案1：Builder 模式的完美链式调用
     */
    private static void solution1_BuilderPattern() {
        System.out.println("【解决方案1】Builder 模式的完美链式调用");
        System.out.println("----------------------------------------");

        // ✅ 完美的链式调用！
        User user = new UserBuilder()
            .setName("Tom")      // 返回 UserBuilder
            .setEmail("tom@example.com")  // 返回 UserBuilder
            .setAge(18)          // 返回 UserBuilder
            .setGender("Male")   // 返回 UserBuilder
            .build();

        System.out.println("✓ 完美的链式调用");
        System.out.println("✓ 每个方法都返回正确的类型");
        System.out.println("✓ 构建的用户: " + user);
        System.out.println();

        // ✅ 也可以构建 Admin
        Admin admin = new AdminBuilder()
            .setName("Alice")
            .setEmail("alice@admin.com")
            .setRole("SUPER_ADMIN")
            .setPermissions("ALL")
            .build();

        System.out.println("✓ 同样适用于 AdminBuilder");
        System.out.println("✓ 构建的管理员: " + admin);
        System.out.println();
    }

    /**
     * 解决方案2：实体类继承的链式赋值
     */
    private static void solution2_EntityInheritance() {
        System.out.println("【解决方案2】实体类继承的链式赋值");
        System.out.println("----------------------------------------");

        // ✅ 链式赋值，类型正确！
        Employee emp = new Employee()
            .setId(1L)               // 返回 Employee
            .setName("Bob")          // 返回 Employee
            .setDepartment("IT")     // 返回 Employee
            .setSalary(50000.0);     // 返回 Employee

        System.out.println("✓ 实体类可以链式赋值了");
        System.out.println("✓ 员工信息: " + emp);
        System.out.println();

        Manager manager = new Manager()
            .setId(2L)
            .setName("Charlie")
            .setDepartment("Sales")
            .setTeamSize(10);

        System.out.println("✓ 子类 Manager 也可以链式调用");
        System.out.println("✓ 经理信息: " + manager);
        System.out.println();
    }

    /**
     * 解决方案3：真实场景 - Enum 枚举就是自限定类型
     */
    private static void solution3_RealWorldExample() {
        System.out.println("【真实案例】Java 枚举就是自限定类型");
        System.out.println("----------------------------------------");

        System.out.println("💡 你知道吗？Enum 的定义就是自限定类型：");
        System.out.println("   public abstract class Enum<E extends Enum<E>>");
        System.out.println();

        System.out.println("这就是为什么：");
        System.out.println("   enum Color { RED, GREEN, BLUE }");
        System.out.println("实际上是：");
        System.out.println("   class Color extends Enum<Color>");
        System.out.println();

        System.out.println("✓ Enum.compareTo(E) 参数类型是 E，而不是 Enum");
        System.out.println("✓ 所以 Color.RED.compareTo(Color.BLUE) 可以编译");
        System.out.println("✓ 但 Color.RED.compareTo(Size.LARGE) 无法编译！");
        System.out.println();
    }

    // ========================================
    // 解决方案代码示例
    // ========================================

    /**
     * ✅ 自限定类型的 Builder 基类
     *
     * 核心语法：<T extends Builder<T>>
     * - T 是类型参数
     * - T 必须是 Builder<T> 的子类
     * - 这样 setXxx 方法返回 T 时，T 就是具体的子类类型
     */
    static abstract class Builder<T extends Builder<T>> {
        protected String name;
        protected String email;

        /**
         * 返回 T（子类类型）而不是 Builder
         */
        @SuppressWarnings("unchecked")
        public T setName(String name) {
            this.name = name;
            return (T) this;  // 强制转换为 T，但是类型安全的
        }

        @SuppressWarnings("unchecked")
        public T setEmail(String email) {
            this.email = email;
            return (T) this;
        }
    }

    /**
     * ✅ UserBuilder 继承时传入自己
     *
     * 语法：extends Builder<UserBuilder>
     * - 告诉编译器：T = UserBuilder
     * - 父类的 setName() 返回的 T 就是 UserBuilder
     */
    static class UserBuilder extends Builder<UserBuilder> {
        private int age;
        private String gender;

        public UserBuilder setAge(int age) {
            this.age = age;
            return this;
        }

        public UserBuilder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public User build() {
            return new User(name, email, age, gender);
        }
    }

    static class AdminBuilder extends Builder<AdminBuilder> {
        private String role;
        private String permissions;

        public AdminBuilder setRole(String role) {
            this.role = role;
            return this;
        }

        public AdminBuilder setPermissions(String permissions) {
            this.permissions = permissions;
            return this;
        }

        public Admin build() {
            return new Admin(name, email, role, permissions);
        }
    }

    // 实体类
    static class User {
        String name;
        String email;
        int age;
        String gender;

        public User(String name, String email, int age, String gender) {
            this.name = name;
            this.email = email;
            this.age = age;
            this.gender = gender;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
        }
    }

    static class Admin {
        String name;
        String email;
        String role;
        String permissions;

        public Admin(String name, String email, String role, String permissions) {
            this.name = name;
            this.email = email;
            this.role = role;
            this.permissions = permissions;
        }

        @Override
        public String toString() {
            return "Admin{name='" + name + "', role='" + role + "'}";
        }
    }

    /**
     * ✅ 自限定类型的实体类基类
     */
    static abstract class BaseEntity<T extends BaseEntity<T>> {
        protected Long id;
        protected String name;

        @SuppressWarnings("unchecked")
        public T setId(Long id) {
            this.id = id;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setName(String name) {
            this.name = name;
            return (T) this;
        }
    }

    static class Employee extends BaseEntity<Employee> {
        private String department;
        private Double salary;

        public Employee setDepartment(String department) {
            this.department = department;
            return this;
        }

        public Employee setSalary(Double salary) {
            this.salary = salary;
            return this;
        }

        @Override
        public String toString() {
            return "Employee{name='" + name + "', department='" + department + "'}";
        }
    }

    static class Manager extends BaseEntity<Manager> {
        private String department;
        private Integer teamSize;

        public Manager setDepartment(String department) {
            this.department = department;
            return this;
        }

        public Manager setTeamSize(Integer teamSize) {
            this.teamSize = teamSize;
            return this;
        }

        @Override
        public String toString() {
            return "Manager{name='" + name + "', teamSize=" + teamSize + "}";
        }
    }
}
