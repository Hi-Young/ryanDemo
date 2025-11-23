package com.geektime.basic.generic.training.day5.after;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 解决方案：TypeToken 模式
 *
 * 核心思想：通过匿名内部类捕获泛型类型信息
 * 原理：子类可以通过反射获取父类的泛型参数
 */
public class TypeTokenDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TypeToken 获取运行时泛型信息");
        System.out.println("========================================\n");

        solution1_BasicTypeToken();
        solution2_CreateGenericArray();
        solution3_ComplexType();

        System.out.println("\n========================================");
        System.out.println("✅ TypeToken 的威力：");
        System.out.println("----------------------------------------");
        System.out.println("1. 可以获取运行时的泛型类型信息");
        System.out.println("2. 可以创建泛型数组");
        System.out.println("3. 支持复杂的嵌套泛型类型");
        System.out.println("========================================");
    }

    /**
     * 解决方案1：TypeToken 基础用法
     */
    private static void solution1_BasicTypeToken() {
        System.out.println("【解决方案1】TypeToken 基础用法");
        System.out.println("----------------------------------------");

        // ✅ 创建 TypeToken 获取类型信息
        TypeToken<String> stringToken = new TypeToken<String>(){};
        TypeToken<List<String>> listToken = new TypeToken<List<String>>(){};
        TypeToken<List<Integer>> intListToken = new TypeToken<List<Integer>>(){};

        System.out.println("✓ String 类型: " + stringToken.getType());
        System.out.println("✓ List<String> 类型: " + listToken.getType());
        System.out.println("✓ List<Integer> 类型: " + intListToken.getType());
        System.out.println();

        // ✅ 可以区分不同的泛型参数
        System.out.println("List<String> 和 List<Integer> 是同一个类型吗？");
        System.out.println("  " + listToken.getType().equals(intListToken.getType()));
        System.out.println("✓ TypeToken 可以区分泛型参数！");
        System.out.println();
    }

    /**
     * 解决方案2：使用 TypeToken 创建泛型数组
     */
    private static void solution2_CreateGenericArray() {
        System.out.println("【解决方案2】使用 TypeToken 创建泛型数组");
        System.out.println("----------------------------------------");

        // ✅ 创建 String 数组
        GenericArray<String> stringArray = new GenericArray<>(
            new TypeToken<String>(){}, 5
        );
        stringArray.set(0, "Hello");
        stringArray.set(1, "World");

        System.out.println("✓ 创建了真正的 String[] 数组");
        System.out.println("✓ stringArray[0] = " + stringArray.get(0));
        System.out.println("✓ stringArray[1] = " + stringArray.get(1));
        System.out.println();

        // ✅ 创建 Integer 数组
        GenericArray<Integer> intArray = new GenericArray<>(
            new TypeToken<Integer>(){}, 3
        );
        intArray.set(0, 100);
        intArray.set(1, 200);

        System.out.println("✓ 创建了真正的 Integer[] 数组");
        System.out.println("✓ intArray[0] = " + intArray.get(0));
        System.out.println();
    }

    /**
     * 解决方案3：处理复杂泛型类型
     */
    private static void solution3_ComplexType() {
        System.out.println("【解决方案3】处理复杂泛型类型");
        System.out.println("----------------------------------------");

        // ✅ 可以表示复杂的嵌套泛型
        TypeToken<List<User>> listOfUser = new TypeToken<List<User>>(){};
        TypeToken<List<List<String>>> nestedList = new TypeToken<List<List<String>>>(){};

        System.out.println("✓ List<User> 类型: " + listOfUser.getType());
        System.out.println("✓ List<List<String>> 类型: " + nestedList.getType());
        System.out.println();

        System.out.println("💡 这就是 Gson 使用的技术：");
        System.out.println("   Type type = new TypeToken<List<User>>(){}.getType();");
        System.out.println("   List<User> users = gson.fromJson(json, type);");
        System.out.println();
    }

    // ========================================
    // TypeToken 实现
    // ========================================

    /**
     * ✅ TypeToken 的核心实现
     *
     * 原理：
     * 1. TypeToken 是抽象类，使用时必须创建子类（匿名内部类）
     * 2. 子类的泛型参数信息会保留在字节码中
     * 3. 通过反射可以获取父类的泛型参数
     */
    public static abstract class TypeToken<T> {
        private final Type type;

        /**
         * 构造函数：通过反射获取泛型参数
         */
        protected TypeToken() {
            // 1. 获取当前类的父类（带泛型参数）
            Type superclass = getClass().getGenericSuperclass();

            // 2. 转换为 ParameterizedType（参数化类型）
            if (superclass instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) superclass;

                // 3. 获取第一个泛型参数（T 的实际类型）
                this.type = parameterized.getActualTypeArguments()[0];
            } else {
                throw new IllegalStateException("必须指定泛型参数");
            }
        }

        /**
         * 获取类型信息
         */
        public Type getType() {
            return type;
        }

        /**
         * 获取原始类型（去掉泛型参数）
         */
        @SuppressWarnings("unchecked")
        public Class<T> getRawType() {
            if (type instanceof Class) {
                return (Class<T>) type;
            } else if (type instanceof ParameterizedType) {
                return (Class<T>) ((ParameterizedType) type).getRawType();
            } else {
                throw new IllegalStateException("无法获取原始类型");
            }
        }

        @Override
        public String toString() {
            return "TypeToken{" + type + "}";
        }
    }

    // ========================================
    // TypeToken 应用示例
    // ========================================

    /**
     * ✅ 使用 TypeToken 创建泛型数组
     */
    static class GenericArray<T> {
        private final T[] array;

        @SuppressWarnings("unchecked")
        public GenericArray(TypeToken<T> typeToken, int size) {
            // 获取原始类型
            Class<T> rawType = typeToken.getRawType();

            // 使用反射创建数组
            this.array = (T[]) Array.newInstance(rawType, size);
        }

        public void set(int index, T value) {
            array[index] = value;
        }

        public T get(int index) {
            return array[index];
        }

        public int length() {
            return array.length;
        }
    }

    /**
     * ✅ 模拟 JSON 解析器
     */
    static class SimpleJsonParser {
        /**
         * 使用 TypeToken 解析 JSON
         */
        public static <T> T parse(String json, TypeToken<T> typeToken) {
            Type type = typeToken.getType();

            System.out.println("解析 JSON 为: " + type);

            // 这里可以根据 type 的信息正确反序列化
            // 如果 type 是 List<User>，可以知道元素类型是 User
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type elementType = pt.getActualTypeArguments()[0];
                System.out.println("  元素类型: " + elementType);
            }

            return null;  // 实际实现会返回解析后的对象
        }
    }

    static class User {
        String name;
        int age;
    }
}
