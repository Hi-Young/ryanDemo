package com.geektime.basic.generic.training.day5.practice.answer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeToken 填空练习 - 参考答案
 *
 * ⚠️ 先自己思考，再看答案！
 */
public class TypeTokenFillInBlanksAnswer {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TypeToken 填空练习 - 参考答案");
        System.out.println("========================================\n");

        test1_BasicTypeToken();
        test2_ComplexType();
        test3_RealWorldUsage();

        System.out.println("\n========================================");
        System.out.println("🎉 所有测试通过！");
        System.out.println("========================================");
    }

    private static void test1_BasicTypeToken() {
        System.out.println("【练习1】基础 TypeToken 实现");
        System.out.println("----------------------------------------");

        TypeToken<String> stringToken = new TypeToken<String>(){};
        TypeToken<Integer> intToken = new TypeToken<Integer>(){};

        System.out.println("✓ String 类型: " + stringToken.getType());
        System.out.println("✓ Integer 类型: " + intToken.getType());
        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案1：TypeToken 的核心实现
     */
    public static abstract class TypeToken<T> {
        private final Type type;

        protected TypeToken() {
            // ✅ 答案：getClass().getGenericSuperclass()
            // 获取当前类的父类（带泛型参数）
            Type superclass = getClass().getGenericSuperclass();

            if (superclass instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) superclass;

                // ✅ 答案：parameterized.getActualTypeArguments()[0]
                // 获取第一个泛型参数
                this.type = parameterized.getActualTypeArguments()[0];
            } else {
                throw new IllegalStateException("必须指定泛型参数");
            }
        }

        public Type getType() {
            return type;
        }

        /**
         * 原理解释：
         *
         * 1. 使用时：new TypeToken<String>(){}
         *    - {} 创建了一个匿名内部类
         *    - 这个匿名类继承了 TypeToken<String>
         *
         * 2. 字节码中会保留：
         *    - class Anonymous$1 extends TypeToken<String>
         *    - 泛型参数 String 被保留了！
         *
         * 3. 反射获取：
         *    - getClass() 返回 Anonymous$1
         *    - getGenericSuperclass() 返回 TypeToken<String>
         *    - getActualTypeArguments() 返回 [String]
         *
         * 4. 关键点：
         *    - 如果直接 new TypeToken<String>() 无法编译（抽象类）
         *    - 即使能编译，类型信息也会被擦除
         *    - 必须通过匿名内部类捕获类型信息
         */
    }

    private static void test2_ComplexType() {
        System.out.println("【练习2】复杂泛型类型");
        System.out.println("----------------------------------------");

        TypeToken<java.util.List<String>> listToken =
            new TypeToken<java.util.List<String>>(){};

        System.out.println("✓ List<String> 类型: " + listToken.getType());
        System.out.println("✓ 测试通过！\n");
    }

    private static void test3_RealWorldUsage() {
        System.out.println("【练习3】真实场景 - 模拟 Gson");
        System.out.println("----------------------------------------");

        String json = "[{\"name\":\"Tom\"}, {\"name\":\"Jerry\"}]";

        TypeToken<java.util.List<User>> typeToken =
            new TypeToken<java.util.List<User>>(){};

        parseJson(json, typeToken);

        System.out.println("✓ 测试通过！\n");
    }

    /**
     * ✅ 答案2：parseJson 方法实现
     */
    private static <T> void parseJson(String json, TypeToken<T> typeToken) {
        // ✅ 答案：typeToken.getType()
        Type type = typeToken.getType();

        System.out.println("  解析 JSON 为: " + type);

        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;

            // ✅ 答案：pt.getActualTypeArguments()[0]
            // 获取第一个泛型参数（List 的元素类型）
            Type elementType = pt.getActualTypeArguments()[0];

            System.out.println("  元素类型: " + elementType);

            // 💡 实际的 JSON 解析器会用这个信息来正确反序列化
            // 比如：
            // - 知道是 List<User>
            // - 解析 JSON 数组中的每个对象为 User
            // - 而不是解析为 Map
        }
    }

    static class User {
        String name;
    }

    // ========================================
    // 思考题答案
    // ========================================
    static {
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│  思考题答案                                 │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  问题1：为什么 TypeToken 必须是抽象类？     │");
        System.out.println("│  答案：强制使用者创建子类（匿名内部类）     │");
        System.out.println("│        只有子类才能通过反射获取泛型参数     │");
        System.out.println("│                                             │");
        System.out.println("│  问题2：为什么使用时要加 {}？              │");
        System.out.println("│  答案：{} 创建了匿名内部类                 │");
        System.out.println("│        new TypeToken<String>(){}            │");
        System.out.println("│        等价于：                             │");
        System.out.println("│        class Anonymous extends              │");
        System.out.println("│              TypeToken<String> {}           │");
        System.out.println("│        new Anonymous()                      │");
        System.out.println("│                                             │");
        System.out.println("│  问题3：如果不加 {}？                       │");
        System.out.println("│  答案：无法编译，因为 TypeToken 是抽象类   │");
        System.out.println("│        即使不是抽象类，泛型信息也会被擦除   │");
        System.out.println("│        无法获取 T 的实际类型                │");
        System.out.println("└─────────────────────────────────────────────┘");

        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│  核心原理总结                               │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  类型擦除的规则：                           │");
        System.out.println("│  1. 泛型参数在运行时被擦除                  │");
        System.out.println("│  2. 但子类继承父类时，泛型参数会保留        │");
        System.out.println("│                                             │");
        System.out.println("│  举例：                                     │");
        System.out.println("│  - List<String> list → 擦除为 List          │");
        System.out.println("│  - class MyList extends                     │");
        System.out.println("│      ArrayList<String>                      │");
        System.out.println("│    → 字节码保留 ArrayList<String>           │");
        System.out.println("│                                             │");
        System.out.println("│  TypeToken 利用这个规则：                   │");
        System.out.println("│  - 创建匿名子类捕获泛型参数                 │");
        System.out.println("│  - 通过反射读取父类的泛型参数               │");
        System.out.println("│  - 绕过类型擦除的限制                       │");
        System.out.println("└─────────────────────────────────────────────┘");
    }
}
