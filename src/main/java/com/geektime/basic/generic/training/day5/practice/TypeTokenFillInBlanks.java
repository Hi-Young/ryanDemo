//package com.geektime.basic.generic.training.day5.practice;
//
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//
///**
// * TypeToken 填空练习
// *
// * 🎯 核心任务：理解 TypeToken 的实现原理
// *
// * 原理：
// * 1. TypeToken 是抽象类，使用时创建匿名内部类
// * 2. 匿名内部类的泛型参数会保留在字节码中
// * 3. 通过反射获取父类的泛型参数
// */
//public class TypeTokenFillInBlanks {
//
//    public static void main(String[] args) {
//        System.out.println("========================================");
//        System.out.println("   TypeToken 填空练习");
//        System.out.println("========================================\n");
//
//        test1_BasicTypeToken();
//        test2_ComplexType();
//        test3_RealWorldUsage();
//
//        System.out.println("\n========================================");
//        System.out.println("🎉 所有测试通过！");
//        System.out.println("========================================");
//    }
//
//    // ========================================
//    // 练习1：基础 TypeToken 实现
//    // ========================================
//    private static void test1_BasicTypeToken() {
//        System.out.println("【练习1】基础 TypeToken 实现");
//        System.out.println("----------------------------------------");
//
//        // 创建 TypeToken
//        TypeToken<String> stringToken = new TypeToken<String>(){};
//        TypeToken<Integer> intToken = new TypeToken<Integer>(){};
//
//        System.out.println("✓ String 类型: " + stringToken.getType());
//        System.out.println("✓ Integer 类型: " + intToken.getType());
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    /**
//     * 🎯 TODO 1: 实现 TypeToken 的核心逻辑
//     *
//     * 需求：通过反射获取泛型参数 T 的实际类型
//     *
//     * 步骤：
//     * 1. 获取当前类的父类（带泛型参数）- getClass().getGenericSuperclass()
//     * 2. 转换为 ParameterizedType
//     * 3. 获取第一个泛型参数
//     *
//     * 提示：
//     * - getGenericSuperclass() 返回 Type
//     * - ParameterizedType 有 getActualTypeArguments() 方法
//     * - getActualTypeArguments()[0] 就是 T 的实际类型
//     */
//    public static abstract class TypeToken<T> {
//        private final Type type;
//
//        protected TypeToken() {
//            // TODO: 填写获取泛型参数的逻辑
//
//            // 1. 获取父类（带泛型参数）
//            Type superclass = ___填写代码___;
//
//            // 2. 判断是否是参数化类型
//            if (superclass instanceof ParameterizedType) {
//                ParameterizedType parameterized = (ParameterizedType) superclass;
//
//                // 3. 获取第一个泛型参数（T 的实际类型）
//                this.type = ___填写代码___;
//            } else {
//                throw new IllegalStateException("必须指定泛型参数");
//            }
//        }
//
//        public Type getType() {
//            return type;
//        }
//    }
//
//    // ========================================
//    // 练习2：复杂泛型类型
//    // ========================================
//    private static void test2_ComplexType() {
//        System.out.println("【练习2】复杂泛型类型");
//        System.out.println("----------------------------------------");
//
//        // TypeToken 可以表示复杂的嵌套类型
//        TypeToken<java.util.List<String>> listToken =
//            new TypeToken<java.util.List<String>>(){};
//
//        System.out.println("✓ List<String> 类型: " + listToken.getType());
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    // ========================================
//    // 练习3：真实场景 - 模拟 Gson
//    // ========================================
//    private static void test3_RealWorldUsage() {
//        System.out.println("【练习3】真实场景 - 模拟 Gson");
//        System.out.println("----------------------------------------");
//
//        String json = "[{\"name\":\"Tom\"}, {\"name\":\"Jerry\"}]";
//
//        // ✅ 使用 TypeToken 传递完整的类型信息
//        TypeToken<java.util.List<User>> typeToken =
//            new TypeToken<java.util.List<User>>(){};
//
//        parseJson(json, typeToken);
//
//        System.out.println("✓ 测试通过！\n");
//    }
//
//    /**
//     * 🎯 TODO 2: 实现 parseJson 方法
//     *
//     * 需求：
//     * 1. 从 TypeToken 获取类型信息
//     * 2. 判断是否是 ParameterizedType
//     * 3. 如果是，获取元素类型
//     */
//    private static <T> void parseJson(String json, TypeToken<T> typeToken) {
//        // TODO: 从 typeToken 获取类型
//        Type type = ___填写代码___;
//
//        System.out.println("  解析 JSON 为: " + type);
//
//        // TODO: 判断是否是参数化类型（比如 List<User>）
//        if (type instanceof ParameterizedType) {
//            ParameterizedType pt = (ParameterizedType) type;
//
//            // TODO: 获取元素类型
//            Type elementType = ___填写代码___;
//
//            System.out.println("  元素类型: " + elementType);
//        }
//    }
//
//    static class User {
//        String name;
//    }
//
//    // ========================================
//    // 思考题
//    // ========================================
//    static {
//        System.out.println("\n┌─────────────────────────────────────────────┐");
//        System.out.println("│  思考题                                     │");
//        System.out.println("├─────────────────────────────────────────────┤");
//        System.out.println("│  问题1：为什么 TypeToken 必须是抽象类？     │");
//        System.out.println("│  问题2：为什么使用时要加 {}？              │");
//        System.out.println("│         new TypeToken<String>(){}           │");
//        System.out.println("│                              ↑↑             │");
//        System.out.println("│  问题3：如果直接 new TypeToken<String>()    │");
//        System.out.println("│         （不加{}），会发生什么？            │");
//        System.out.println("│                                             │");
//        System.out.println("│  提示：想想匿名内部类的作用                 │");
//        System.out.println("└─────────────────────────────────────────────┘");
//    }
//}
