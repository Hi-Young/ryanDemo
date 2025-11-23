package com.geektime.basic.generic.training.day5.before;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题演示：类型擦除导致的运行时类型信息丢失
 *
 * 场景：
 * 1. 无法创建泛型数组
 * 2. 无法在运行时获取泛型参数类型
 * 3. JSON 反序列化时无法知道具体类型
 */
public class TypeErasureProblem {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   类型擦除导致的运行时类型丢失");
        System.out.println("========================================\n");

//        problem1_CannotCreateGenericArray();
//        problem2_CannotGetGenericType();
        problem3_JsonDeserializationProblem();

        System.out.println("\n========================================");
        System.out.println("💔 核心问题：");
        System.out.println("----------------------------------------");
        System.out.println("泛型信息在编译后被擦除，运行时无法获取");
        System.out.println("\n🎯 解决方案：");
        System.out.println("1. 传入 Class<T> 对象（适合简单类型）");
        System.out.println("2. TypeToken 模式（适合复杂泛型类型）");
        System.out.println("========================================");
    }

    /**
     * 问题1：无法创建泛型数组
     */
    private static void problem1_CannotCreateGenericArray() {
        System.out.println("【问题1】无法创建泛型数组");
        System.out.println("----------------------------------------");

        // ❌ 直接创建泛型数组 - 编译错误
//         GenericArray<String> arr = new GenericArray<>(5);
//         arr.set(0, "Hello");

        System.out.println("✗ 无法写出：T[] array = new T[size];");
        System.out.println("✗ 因为运行时不知道 T 是什么类型");
        System.out.println();

        // 😢 只能用 Object[] 然后强制转换
        System.out.println("✗ 只能用 Object[] + 强制转换");
        System.out.println("✗ 会有警告且不安全");
        System.out.println();
    }

    /**
     * ❌ 无法编译的代码
     */
//    static class GenericArray<T> {
//        private T[] array;
//
//        public GenericArray(int size) {
//            // ❌ 编译错误：Cannot create a generic array of T
//            this.array = new T[size];
//        }
//
//        public void set(int index, T value) {
//            array[index] = value;
//        }
//
//        public T get(int index) {
//            return array[index];
//        }
//    }

    /**
     * 问题2：无法在运行时获取泛型参数类型
     */
    private static void problem2_CannotGetGenericType() {
        System.out.println("【问题2】无法在运行时获取泛型参数类型");
        System.out.println("----------------------------------------");

        List<String> stringList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // 运行时都是同一个类型
        System.out.println("List<String> 和 List<Integer> 在运行时是同一个类？");
        System.out.println("  " + (stringList.getClass() == intList.getClass()));
        System.out.println();

        System.out.println("✗ 运行时无法区分 List<String> 和 List<Integer>");
        System.out.println("✗ 泛型信息被擦除，都变成了 List");
        System.out.println();
    }

    /**
     * 问题3：JSON 反序列化的类型问题
     */
    private static void problem3_JsonDeserializationProblem() {
        System.out.println("【问题3】JSON 反序列化无法知道泛型参数");
        System.out.println("----------------------------------------");

        String json = "[{\"name\":\"Tom\",\"age\":18}, {\"name\":\"Jerry\",\"age\":20}]";

        // ❌ 方式1：只传 Class<T>，无法表示 List<User>
         List<User> users = fromJson(json, List.class);
        System.out.println(users);
        // 编译通过，但运行时 users 里是 Map，不是 User！

        System.out.println("✗ 想反序列化为 List<User>");
        System.out.println("✗ 但 fromJson(json, List.class) 只知道是 List");
        System.out.println("✗ 不知道元素类型是 User");
        System.out.println();

        // ❌ 方式2：无法获取 List<User>.class
        // Class<List<User>> clazz = List<User>.class;  // ❌ 语法错误！

        System.out.println("✗ 无法写 List<User>.class");
        System.out.println("✗ Java 不允许这样的语法");
        System.out.println();

        System.out.println("💡 Gson 的解决方案：");
        System.out.println("   Type type = new TypeToken<List<User>>(){}.getType();");
        System.out.println("   List<User> users = gson.fromJson(json, type);");
        System.out.println();
    }

    /**
     * 模拟的 fromJson 方法
     */
    private static <T> T fromJson(String json, Class<T> clazz) {
        // 问题：只知道 T 是 List，不知道元素类型
        System.out.println("只能知道: " + clazz.getName());
        System.out.println("无法知道: List 的元素类型");
        return null;
    }

    /**
     * 实际问题演示：没有 TypeToken 时的困境
     */
    static class SimpleJsonParser {
        /**
         * ❌ 这个方法有问题：无法正确反序列化泛型集合
         */
        public static <T> T parse(String json, Class<T> clazz) {
            // 如果 T 是 List<User>，这里只能知道是 List
            // 无法知道元素类型是 User
            // 所以反序列化出来的可能是 List<Map>，而不是 List<User>

            System.out.println("解析类型: " + clazz.getSimpleName());
            System.out.println("✗ 但无法知道泛型参数是什么");

            return null;
        }
    }

    static class User {
        String name;
        int age;
    }
}
