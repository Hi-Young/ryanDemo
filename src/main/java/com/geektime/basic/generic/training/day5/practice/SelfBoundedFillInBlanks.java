//package com.geektime.basic.generic.training.day5.practice;
//
///**
// * 自限定类型填空练习
// *
// * 🎯 核心任务：理解并填写自限定类型的语法
// *
// * 核心语法：
// * - class Base<T extends Base<T>>
// * - class Derived extends Base<Derived>
// */
//public class SelfBoundedFillInBlanks {
//
//    public static void main(String[] args) {
//        System.out.println("========================================");
//        System.out.println("   自限定类型填空练习");
//        System.out.println("========================================\n");
//
//        test1_SimpleBuilder();
//        test2_EntityChain();
//        test3_FluentAPI();
//
//        System.out.println("\n========================================");
//        System.out.println("🎉 所有测试通过！");
//        System.out.println("========================================");
//    }
//
//    // ========================================
//    // 练习1：简单的 Builder 模式
//    // ========================================
//    private static void test1_SimpleBuilder() {
//        System.out.println("【练习1】简单的 Builder 模式");
//        System.out.println("----------------------------------------");
//
//        // 目标：链式调用返回正确的类型
//        Product product = new ProductBuilder()
//            .setName("iPhone")
//            .setPrice(999.0)
//            .setCategory("Electronics")
//            .build();
//
//        System.out.println("✓ 构建的产品: " + product);
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    /**
//     * 🎯 TODO 1: 填写 Builder 的自限定类型
//     *
//     * 需求：让 setName() 和 setPrice() 返回子类类型（而不是 Builder）
//     *
//     * 问题1：Builder 的类型参数应该怎么写？
//     * A. <T>
//     * B. <T extends Builder>
//     * C. <T extends Builder<T>>
//     * D. <T super Builder<T>>
//     *
//     * 问题2：setName() 的返回类型应该是？
//     * A. Builder
//     * B. Builder<T>
//     * C. T
//     *
//     * 答案：在下面填写
//     */
//    static abstract class Builder<___填写类型参数___> {
//        protected String name;
//        protected Double price;
//
//        // TODO: 填写返回类型
//        public ___填写返回类型___ setName(String name) {
//            this.name = name;
//            return (T) this;  // 假设你填的类型参数是 T
//        }
//
//        // TODO: 填写返回类型
//        public ___填写返回类型___ setPrice(Double price) {
//            this.price = price;
//            return (T) this;
//        }
//    }
//
//    /**
//     * 🎯 TODO 2: 填写 ProductBuilder 的继承
//     *
//     * 问题：ProductBuilder 应该如何继承 Builder？
//     * A. extends Builder
//     * B. extends Builder<Product>
//     * C. extends Builder<ProductBuilder>
//     * D. extends Builder<? extends ProductBuilder>
//     *
//     * 答案：在下面填写
//     */
//    static class ProductBuilder extends Builder<___填写继承参数___> {
//        private String category;
//
//        public ProductBuilder setCategory(String category) {
//            this.category = category;
//            return this;
//        }
//
//        public Product build() {
//            Product p = new Product();
//            p.name = this.name;
//            p.price = this.price;
//            p.category = this.category;
//            return p;
//        }
//    }
//
//    static class Product {
//        String name;
//        Double price;
//        String category;
//
//        @Override
//        public String toString() {
//            return "Product{name='" + name + "', price=" + price + ", category='" + category + "'}";
//        }
//    }
//
//    // ========================================
//    // 练习2：实体类的链式赋值
//    // ========================================
//    private static void test2_EntityChain() {
//        System.out.println("【练习2】实体类的链式赋值");
//        System.out.println("----------------------------------------");
//
//        // 目标：链式调用所有setter方法
//        Student student = new Student()
//            .setId(1L)
//            .setName("Tom")
//            .setGrade(90)
//            .setMajor("Computer Science");
//
//        System.out.println("✓ 学生信息: " + student);
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    /**
//     * 🎯 TODO 3: 填写 Entity 基类的自限定类型
//     *
//     * 提示：和 Builder 的模式一样
//     */
//    static abstract class Entity<___填写类型参数___> {
//        protected Long id;
//        protected String name;
//
//        // TODO: 填写返回类型，让它返回子类类型
//        public ___填写返回类型___ setId(Long id) {
//            this.id = id;
//            return (T) this;
//        }
//
//        public ___填写返回类型___ setName(String name) {
//            this.name = name;
//            return (T) this;
//        }
//    }
//
//    /**
//     * 🎯 TODO 4: 填写 Student 的继承
//     */
//    static class Student extends Entity<___填写继承参数___> {
//        private Integer grade;
//        private String major;
//
//        public Student setGrade(Integer grade) {
//            this.grade = grade;
//            return this;
//        }
//
//        public Student setMajor(String major) {
//            this.major = major;
//            return this;
//        }
//
//        @Override
//        public String toString() {
//            return "Student{name='" + name + "', grade=" + grade + ", major='" + major + "'}";
//        }
//    }
//
//    // ========================================
//    // 练习3：Fluent API（挑战题）⭐⭐⭐
//    // ========================================
//    private static void test3_FluentAPI() {
//        System.out.println("【练习3】Fluent API（挑战题）");
//        System.out.println("----------------------------------------");
//
//        // 目标：实现流畅的 API 调用
//        Query query = new Query()
//            .select("name", "age")
//            .from("users")
//            .where("age > 18")
//            .orderBy("name")
//            .limit(10);
//
//        System.out.println("✓ 构建的查询: " + query);
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    /**
//     * 🎯 TODO 5: 实现 Fluent API
//     *
//     * 挑战：
//     * 1. BaseQuery 使用自限定类型
//     * 2. Query 继承 BaseQuery
//     * 3. 所有方法都返回 this，支持链式调用
//     *
//     * 思考：为什么需要自限定类型？
//     * 答：如果 BaseQuery 的方法返回 BaseQuery，那么子类 Query 的特有方法就无法链式调用
//     */
//    static abstract class BaseQuery<___填写类型参数___> {
//        protected String selectClause = "";
//        protected String fromClause = "";
//        protected String whereClause = "";
//
//        // TODO: 填写返回类型
//        public ___填写返回类型___ select(String... columns) {
//            this.selectClause = "SELECT " + String.join(", ", columns);
//            return (T) this;
//        }
//
//        public ___填写返回类型___ from(String table) {
//            this.fromClause = " FROM " + table;
//            return (T) this;
//        }
//
//        public ___填写返回类型___ where(String condition) {
//            this.whereClause = " WHERE " + condition;
//            return (T) this;
//        }
//    }
//
//    /**
//     * 🎯 TODO 6: 填写 Query 的继承
//     */
//    static class Query extends BaseQuery<___填写继承参数___> {
//        private String orderByClause = "";
//        private Integer limitValue = null;
//
//        public Query orderBy(String column) {
//            this.orderByClause = " ORDER BY " + column;
//            return this;
//        }
//
//        public Query limit(int n) {
//            this.limitValue = n;
//            return this;
//        }
//
//        @Override
//        public String toString() {
//            String sql = selectClause + fromClause + whereClause + orderByClause;
//            if (limitValue != null) {
//                sql += " LIMIT " + limitValue;
//            }
//            return sql;
//        }
//    }
//}
