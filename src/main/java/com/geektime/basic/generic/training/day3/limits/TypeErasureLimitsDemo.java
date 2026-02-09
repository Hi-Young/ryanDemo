package com.geektime.basic.generic.training.day3.limits;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 演示：类型擦除（Type Erasure）带来的限制
 *
 * 🎯 理解泛型的底层机制和限制
 *
 * 核心概念：
 * Java 的泛型是"伪泛型"，只在编译期存在，运行时会被擦除。
 * - 编译后：List<String> 和 List<Integer> 都变成 List
 * - 所有的 T 都被替换成 Object 或其上界
 */
public class TypeErasureLimitsDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   类型擦除的限制与解决方案");
        System.out.println("========================================\n");

        limit1_CannotCreateInstance();
        limit2_CannotCreateArray();
        limit3_CannotUseInstanceof();
        limit4_CannotUseInStaticContext();
        limit5_TypeErasureExample();

        System.out.println("\n========================================");
        System.out.println("💡 核心要点：");
        System.out.println("----------------------------------------");
        System.out.println("1. 泛型只在编译期存在，运行时会被擦除");
        System.out.println("2. 不能 new T()、new T[]、T.class");
        System.out.println("3. 静态方法/字段不能使用类的类型参数");
        System.out.println("4. 解决方案：传入 Class<T> 对象");
        System.out.println("========================================");
    }

    // ========================================
    // 限制1：不能创建泛型对象
    // ========================================
    private static <T> void limit1_CannotCreateInstance() {
        System.out.println("【限制1】不能创建泛型对象");
        System.out.println("----------------------------------------");

        System.out.println("❌ 以下代码无法编译：");
        System.out.println("    T obj = new T();");
        System.out.println();
        System.out.println("原因：编译器不知道 T 的具体类型，无法调用构造函数");
        System.out.println();

        System.out.println("✅ 解决方案1：传入 Class<T> 对象");
        Container<String> container1 = new Container<>(String.class);
        String str = container1.createInstance();
        System.out.println("  - 创建的对象: " + str + " (类型: " + str.getClass().getSimpleName() + ")");

        System.out.println();
        System.out.println("✅ 解决方案2：使用工厂模式");
        System.out.println("  - 传入一个能创建对象的函数（Java 8+ 可用 Supplier<T>）");
        System.out.println();
    }

    /**
     * 演示如何通过 Class<T> 创建泛型对象
     */
    static class Container<T> {
        private Class<T> type;

        public Container(Class<T> type) {
            this.type = type;
        }

        /**
         * ✅ 使用 Class.newInstance() 创建对象
         */
        public T createInstance() {
            try {
                return type.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("无法创建实例: " + e.getMessage());
            }
        }
    }

    // ========================================
    // 限制2：不能创建泛型数组
    // ========================================
    private static void limit2_CannotCreateArray() {
        System.out.println("【限制2】不能创建泛型数组");
        System.out.println("----------------------------------------");

        System.out.println("❌ 以下代码无法编译：");
        System.out.println("    T[] array = new T[10];");
        System.out.println();
        System.out.println("原因：类型擦除后变成 new Object[10]，无法转换为 T[]");
        System.out.println();

        System.out.println("✅ 解决方案1：使用 Object[] 然后强制转换");
        GenericArray<String> array1 = new GenericArray<>(5);
        array1.set(0, "Hello");
        System.out.println("  - 获取元素: " + array1.get(0));

        System.out.println();
        System.out.println("✅ 解决方案2：使用 ArrayList 代替数组");
        List<String> list = new ArrayList<>();
        list.add("Hello");
        System.out.println("  - List方式: " + list.get(0));

        System.out.println();
        System.out.println("✅ 解决方案3：使用 Array.newInstance()");
        GenericArraySafe<Integer> array2 = new GenericArraySafe<>(Integer.class, 5);
        array2.set(0, 100);
        System.out.println("  - 安全方式: " + array2.get(0));

        System.out.println();
    }

    /**
     * 解决方案1：使用 Object[] + 强制转换
     * ⚠️ 会有编译警告
     */
    static class GenericArray<T> {
        private Object[] array;

        @SuppressWarnings("unchecked")
        public GenericArray(int size) {
            array = new Object[size];  // 只能创建 Object[]
        }

        public void set(int index, T value) {
            array[index] = value;
        }

        @SuppressWarnings("unchecked")
        public T get(int index) {
            return (T) array[index];  // 强制转换
        }
    }

    /**
     * 解决方案3：使用 Array.newInstance() 创建真正的泛型数组
     * ✅ 类型安全
     */
    static class GenericArraySafe<T> {
        private T[] array;

        @SuppressWarnings("unchecked")
        public GenericArraySafe(Class<T> type, int size) {
            // 使用反射创建真正的 T[] 数组
            array = (T[]) Array.newInstance(type, size);
        }

        public void set(int index, T value) {
            array[index] = value;
        }

        public T get(int index) {
            return array[index];
        }
    }

    // ========================================
    // 限制3：不能用 instanceof 判断泛型类型
    // ========================================
    private static void limit3_CannotUseInstanceof() {
        System.out.println("【限制3】不能用 instanceof 判断泛型类型");
        System.out.println("----------------------------------------");

        System.out.println("❌ 以下代码无法编译：");
        System.out.println("    if (obj instanceof T) { ... }");
        System.out.println("    if (obj instanceof List<String>) { ... }");
        System.out.println();
        System.out.println("原因：运行时泛型信息已被擦除");
        System.out.println();

        System.out.println("✅ 可以这样写：");
        Object obj = "Hello";
        if (obj instanceof String) {  // ✓ 判断具体类型
            System.out.println("  - obj 是 String 类型");
        }

        List<String> list = new ArrayList<>();
        if (list instanceof List) {  // ✓ 不带泛型参数
            System.out.println("  - list 是 List 类型（但不知道是 List<String> 还是 List<Integer>）");
        }

        System.out.println();
    }

    // ========================================
    // 限制4：静态方法/字段不能使用类的类型参数
    // ========================================
    private static void limit4_CannotUseInStaticContext() {
        System.out.println("【限制4】静态上下文不能使用类的类型参数");
        System.out.println("----------------------------------------");

        System.out.println("❌ 以下代码无法编译：");
        System.out.println("    class Box<T> {");
        System.out.println("        private static T value;       // ❌ 错误");
        System.out.println("        public static T getValue() {  // ❌ 错误");
        System.out.println("            return value;");
        System.out.println("        }");
        System.out.println("    }");
        System.out.println();
        System.out.println("原因：静态成员属于类，而类型参数属于实例");
        System.out.println("      Box<String> 和 Box<Integer> 共享同一个静态成员");
        System.out.println();

        System.out.println("✅ 解决方案：静态泛型方法有自己的类型参数");
        String result = StaticGenericDemo.identity("Hello");
        System.out.println("  - 静态泛型方法返回: " + result);

        System.out.println();
    }

    static class StaticGenericDemo {
        /**
         * ✅ 静态泛型方法：有自己独立的类型参数
         */
        public static <T> T identity(T value) {
            return value;
        }

        // ❌ 错误示例（无法编译）：
        // private static T value;
        // public static T getValue() { return value; }
    }

    // ========================================
    // 限制5：类型擦除示例
    // ========================================
    private static void limit5_TypeErasureExample() {
        System.out.println("【限制5】类型擦除示例");
        System.out.println("----------------------------------------");

        List<String> stringList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();
//        intList = stringList;

        System.out.println("List<String> 和 List<Integer> 在运行时是同一个类型吗？");
        System.out.println("  - stringList.getClass() == intList.getClass(): "
                + (stringList.getClass() == intList.getClass()));
        System.out.println("  - 类名: " + stringList.getClass().getName());
        System.out.println();

        System.out.println("原因：泛型在编译后被擦除，运行时都是 java.util.ArrayList");
        System.out.println();

        System.out.println("类型擦除规则：");
        System.out.println("  - <T> 擦除后变成 Object");
        System.out.println("  - <T extends Number> 擦除后变成 Number");
        System.out.println("  - <T extends Comparable<T>> 擦除后变成 Comparable");
        System.out.println();
    }
}
