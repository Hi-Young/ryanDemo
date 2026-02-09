//package com.geektime.basic.generic.training.day5.practice.answer;
//
///**
// * 自限定类型填空练习 - 参考答案
// *
// * ⚠️ 先自己思考，再看答案！
// */
//public class SelfBoundedFillInBlanksAnswer {
//
//    public static void main(String[] args) {
//        System.out.println("========================================");
//        System.out.println("   自限定类型填空练习 - 参考答案");
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
//    private static void test1_SimpleBuilder() {
//        System.out.println("【练习1】简单的 Builder 模式");
//        System.out.println("----------------------------------------");
//
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
//     * ✅ 答案1：<T extends Builder<T>>
//     *
//     * 解释：
//     * - T 是类型参数
//     * - T 必须是 Builder<T> 的子类
//     * - 这样 T 就是"自限定"的
//     *
//     * 为什么不能是其他选项？
//     * - <T>：太宽泛，T 可以是任何类型
//     * - <T extends Builder>：缺少泛型参数，Builder 应该是 Builder<T>
//     * - <T super Builder<T>>：语法错误，类型参数只能用 extends
//     */
//    static abstract class Builder<T extends Builder<T>> {
//        protected String name;
//        protected Double price;
//
//        /**
//         * ✅ 答案2：返回类型是 T
//         *
//         * 解释：
//         * - T 代表子类的类型
//         * - ProductBuilder 继承 Builder<ProductBuilder> 时，T = ProductBuilder
//         * - 所以这个方法返回 ProductBuilder，而不是 Builder
//         */
//        @SuppressWarnings("unchecked")
//        public T setName(String name) {
//            this.name = name;
//            return (T) this;
//        }
//
//        @SuppressWarnings("unchecked")
//        public T setPrice(Double price) {
//            this.price = price;
//            return (T) this;
//        }
//    }
//
//    /**
//     * ✅ 答案3：extends Builder<ProductBuilder>
//     *
//     * 解释：
//     * - 继承 Builder，并把自己的类型传进去
//     * - 告诉编译器：T = ProductBuilder
//     * - 这样父类的 setName() 返回的 T 就是 ProductBuilder
//     *
//     * 为什么要"传入自己"？
//     * - 这是自限定类型的关键！
//     * - 让父类知道子类的具体类型
//     * - 这样父类方法就能返回子类类型
//     */
//    static class ProductBuilder extends Builder<ProductBuilder> {
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
//    private static void test2_EntityChain() {
//        System.out.println("【练习2】实体类的链式赋值");
//        System.out.println("----------------------------------------");
//
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
//     * ✅ 答案4：和 Builder 一样的模式
//     */
//    static abstract class Entity<T extends Entity<T>> {
//        protected Long id;
//        protected String name;
//
//        @SuppressWarnings("unchecked")
//        public T setId(Long id) {
//            this.id = id;
//            return (T) this;
//        }
//
//        @SuppressWarnings("unchecked")
//        public T setName(String name) {
//            this.name = name;
//            return (T) this;
//        }
//    }
//
//    /**
//     * ✅ 答案5：extends Entity<Student>
//     */
//    static class Student extends Entity<Student> {
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
//    private static void test3_FluentAPI() {
//        System.out.println("【练习3】Fluent API（挑战题）");
//        System.out.println("----------------------------------------");
//
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
//     * ✅ 答案6：BaseQuery<T extends BaseQuery<T>>
//     *
//     * 核心思想：
//     * - BaseQuery 的方法返回 T（子类类型）
//     * - Query 继承时传入 Query 自己
//     * - 这样 select().from().where() 每一步都返回 Query
//     * - 最后才能调用 Query 特有的 orderBy() 和 limit()
//     */
//    static abstract class BaseQuery<T extends BaseQuery<T>> {
//        protected String selectClause = "";
//        protected String fromClause = "";
//        protected String whereClause = "";
//
//        @SuppressWarnings("unchecked")
//        public T select(String... columns) {
//            this.selectClause = "SELECT " + String.join(", ", columns);
//            return (T) this;
//        }
//
//        @SuppressWarnings("unchecked")
//        public T from(String table) {
//            this.fromClause = " FROM " + table;
//            return (T) this;
//        }
//
//        @SuppressWarnings("unchecked")
//        public T where(String condition) {
//            this.whereClause = " WHERE " + condition;
//            return (T) this;
//        }
//    }
//
//    /**
//     * ✅ 答案7：extends BaseQuery<Query>
//     */
//    static class Query extends BaseQuery<Query> {
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
//
//    // ========================================
//    // 总结
//    // ========================================
//    static {
//        System.out.println("\n┌─────────────────────────────────────────────┐");
//        System.out.println("│  自限定类型的核心模式                       │");
//        System.out.println("├─────────────────────────────────────────────┤");
//        System.out.println("│  1. 父类定义：                              │");
//        System.out.println("│     class Base<T extends Base<T>>           │");
//        System.out.println("│                                             │");
//        System.out.println("│  2. 子类继承：                              │");
//        System.out.println("│     class Child extends Base<Child>         │");
//        System.out.println("│                                             │");
//        System.out.println("│  3. 方法返回：                              │");
//        System.out.println("│     public T doSomething() {                │");
//        System.out.println("│         return (T) this;                    │");
//        System.out.println("│     }                                       │");
//        System.out.println("│                                             │");
//        System.out.println("│  关键要点：                                 │");
//        System.out.println("│  - 子类继承时"传入自己"                    │");
//        System.out.println("│  - 父类方法返回 T（子类类型）               │");
//        System.out.println("│  - 实现完美的链式调用                       │");
//        System.out.println("└─────────────────────────────────────────────┘");
//    }
//}
