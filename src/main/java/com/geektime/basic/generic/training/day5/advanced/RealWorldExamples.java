package com.geektime.basic.generic.training.day5.advanced;

import java.io.Serializable;

/**
 * 真实项目中的自限定类型应用
 *
 * 涵盖：
 * 1. Lombok @Builder 的底层原理
 * 2. JPA 实体类的继承
 * 3. 流畅的查询 DSL
 * 4. Enum 枚举的实现
 */
public class RealWorldExamples {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   真实项目中的自限定类型");
        System.out.println("========================================\n");

        example1_LombokBuilder();
        example2_JPAEntity();
        example3_QueryDSL();
        example4_EnumPattern();

        System.out.println("\n========================================");
        System.out.println("💡 自限定类型在真实项目中无处不在！");
        System.out.println("========================================");
    }

    /**
     * 示例1：Lombok @Builder 的底层原理
     */
    private static void example1_LombokBuilder() {
        System.out.println("【示例1】Lombok @Builder 的底层原理");
        System.out.println("----------------------------------------");

        // Lombok 生成的代码就是自限定类型
        Person person = Person.builder()
            .name("Tom")
            .age(18)
            .email("tom@example.com")
            .build();

        System.out.println("✓ 构建的对象: " + person);
        System.out.println();
        System.out.println("💡 Lombok 生成的代码：");
        System.out.println("   public static class PersonBuilder {");
        System.out.println("       public PersonBuilder name(String name) {");
        System.out.println("           this.name = name;");
        System.out.println("           return this;  // 返回 this");
        System.out.println("       }");
        System.out.println("   }");
        System.out.println();
        System.out.println("💡 如果要支持继承，Lombok 会生成：");
        System.out.println("   abstract class Builder<T extends Builder<T>>");
        System.out.println();
    }

    /**
     * Lombok 风格的 Builder（简化版）
     */
    static class Person {
        private String name;
        private Integer age;
        private String email;

        public static PersonBuilder builder() {
            return new PersonBuilder();
        }

        static class PersonBuilder {
            private String name;
            private Integer age;
            private String email;

            public PersonBuilder name(String name) {
                this.name = name;
                return this;
            }

            public PersonBuilder age(Integer age) {
                this.age = age;
                return this;
            }

            public PersonBuilder email(String email) {
                this.email = email;
                return this;
            }

            public Person build() {
                Person p = new Person();
                p.name = this.name;
                p.age = this.age;
                p.email = this.email;
                return p;
            }
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }

    /**
     * 示例2：JPA 实体类的继承
     */
    private static void example2_JPAEntity() {
        System.out.println("【示例2】JPA 实体类的继承");
        System.out.println("----------------------------------------");

        // JPA 实体类的链式调用
        Article article = new Article()
            .setId(1L)
            .setCreatedBy("admin")
            .setTitle("Java 泛型详解")
            .setContent("泛型是 Java 5 引入的特性...");

        System.out.println("✓ 创建的文章: " + article);
        System.out.println();
        System.out.println("💡 实际项目中的应用：");
        System.out.println("   @MappedSuperclass");
        System.out.println("   abstract class BaseEntity<T extends BaseEntity<T>> {");
        System.out.println("       @Id");
        System.out.println("       private Long id;");
        System.out.println("       ");
        System.out.println("       public T setId(Long id) {");
        System.out.println("           this.id = id;");
        System.out.println("           return (T) this;");
        System.out.println("       }");
        System.out.println("   }");
        System.out.println();
    }

    /**
     * JPA 实体基类（自限定类型）
     */
    static abstract class BaseEntity<T extends BaseEntity<T>> implements Serializable {
        protected Long id;
        protected String createdBy;
        protected String updatedBy;

        @SuppressWarnings("unchecked")
        public T setId(Long id) {
            this.id = id;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setUpdatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return (T) this;
        }
    }

    static class Article extends BaseEntity<Article> {
        private String title;
        private String content;

        public Article setTitle(String title) {
            this.title = title;
            return this;
        }

        public Article setContent(String content) {
            this.content = content;
            return this;
        }

        @Override
        public String toString() {
            return "Article{id=" + id + ", title='" + title + "'}";
        }
    }

    /**
     * 示例3：流畅的查询 DSL（类似 MyBatis-Plus）
     */
    private static void example3_QueryDSL() {
        System.out.println("【示例3】流畅的查询 DSL");
        System.out.println("----------------------------------------");

        // 类似 MyBatis-Plus 的查询写法
        String sql = new UserQuery()
            .select("id", "name", "age")
            .from("users")
            .where("age > 18")
            .andWhere("status = 1")
            .orderBy("created_at DESC")
            .limit(10)
            .toSQL();

        System.out.println("✓ 生成的 SQL: " + sql);
        System.out.println();
        System.out.println("💡 MyBatis-Plus 的 LambdaQueryWrapper 就是这个原理");
        System.out.println();
    }

    /**
     * 查询 DSL 基类
     */
    static abstract class QueryBuilder<T extends QueryBuilder<T>> {
        protected StringBuilder sql = new StringBuilder();

        @SuppressWarnings("unchecked")
        public T select(String... columns) {
            sql.append("SELECT ").append(String.join(", ", columns));
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T from(String table) {
            sql.append(" FROM ").append(table);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T where(String condition) {
            sql.append(" WHERE ").append(condition);
            return (T) this;
        }

        public String toSQL() {
            return sql.toString();
        }
    }

    static class UserQuery extends QueryBuilder<UserQuery> {
        public UserQuery andWhere(String condition) {
            sql.append(" AND ").append(condition);
            return this;
        }

        public UserQuery orderBy(String column) {
            sql.append(" ORDER BY ").append(column);
            return this;
        }

        public UserQuery limit(int n) {
            sql.append(" LIMIT ").append(n);
            return this;
        }
    }

    /**
     * 示例4：Enum 枚举的自限定类型
     */
    private static void example4_EnumPattern() {
        System.out.println("【示例4】Enum 枚举就是自限定类型");
        System.out.println("----------------------------------------");

        System.out.println("💡 Java 枚举的实际定义：");
        System.out.println("   public abstract class Enum<E extends Enum<E>>");
        System.out.println();

        System.out.println("💡 当你写：");
        System.out.println("   enum Color { RED, GREEN, BLUE }");
        System.out.println();

        System.out.println("💡 实际上是：");
        System.out.println("   class Color extends Enum<Color> {");
        System.out.println("       public static final Color RED = new Color();");
        System.out.println("       public static final Color GREEN = new Color();");
        System.out.println("       public static final Color BLUE = new Color();");
        System.out.println("   }");
        System.out.println();

        System.out.println("💡 为什么要用自限定类型？");
        System.out.println("   - compareTo(E o) 的参数是 E，而不是 Enum");
        System.out.println("   - 所以 Color.RED.compareTo(Color.BLUE) 可以编译");
        System.out.println("   - 但 Color.RED.compareTo(Size.LARGE) 无法编译");
        System.out.println("   - 确保类型安全！");
        System.out.println();
    }

    // ========================================
    // 总结
    // ========================================
    static {
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│  自限定类型在真实项目中的应用               │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  1. Lombok @Builder                         │");
        System.out.println("│     - 生成流畅的建造者模式                  │");
        System.out.println("│     - 支持继承的 Builder                    │");
        System.out.println("│                                             │");
        System.out.println("│  2. JPA/Hibernate 实体类                    │");
        System.out.println("│     - BaseEntity 的链式 setter              │");
        System.out.println("│     - 返回子类类型，支持继续链式调用        │");
        System.out.println("│                                             │");
        System.out.println("│  3. MyBatis-Plus QueryWrapper               │");
        System.out.println("│     - 流畅的查询 API                        │");
        System.out.println("│     - 链式调用构建 SQL                      │");
        System.out.println("│                                             │");
        System.out.println("│  4. Java Enum 枚举                          │");
        System.out.println("│     - Enum<E extends Enum<E>>               │");
        System.out.println("│     - 保证 compareTo 的类型安全             │");
        System.out.println("│                                             │");
        System.out.println("│  核心价值：                                 │");
        System.out.println("│  - 完美的链式调用                           │");
        System.out.println("│  - 编译时类型安全                           │");
        System.out.println("│  - 优雅的 API 设计                          │");
        System.out.println("└─────────────────────────────────────────────┘");
    }
}
