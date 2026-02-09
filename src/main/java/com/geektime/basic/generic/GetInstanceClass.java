package com.geektime.basic.generic;

import java.util.ArrayList;
import java.util.List;

public class GetInstanceClass<T> {

    public static <T> T getInstance(Class<T> clazz) {
        try {
            T t = clazz.getDeclaredConstructor().newInstance();
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <T> List<T> castList(List<Object> list, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Object obj : list) {
            // 使用 clazz.cast() 更安全，不会抛出 ClassCastException（而是 RuntimeException）
            try {
                T t = clazz.cast(obj);
                result.add(t);
            } catch (ClassCastException e) {
                // 如果转换失败，可以选择忽略或记录
                System.out.println("转换失败: " + obj);
            }
        }
        return result;
    }

    // 测试代码：
    public static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        list.add("Hello");
        list.add("World");
        list.add(123); // 故意添加不匹配的类型

        List<String> stringList = castList(list, String.class);
        System.out.println(stringList); // 输出：[Hello, World]

        List<Double> doubleList = new ArrayList<>();
        doubleList.add(1.5);
        doubleList.add(2.5);

//        dangerousMethod(doubleList);  // 传入Double列表

        // 问题来了：现在doubleList里混入了Integer！
        for (Double d : doubleList) {  // 💥 ClassCastException！
            System.out.println(d);
        }
    }

    public T getInstance() {
        return null;
    }

    public void dangerousMethod(List<? extends Number> list) {
//        list.add(100);  // 假设这行代码合法...
    }

//    public void dangerousMethod(List<? extends Number> list) {
    // 编译器在这里面临的困境：
    // "我不知道list到底是List<Integer>还是List<Double>"
    // "如果我允许添加任何Number子类..."

//        list.add(100);    // 假设允许添加Integer
//        list.add(1.5);    // 假设允许添加Double  
//        list.add(1.1f);   // 假设允许添加Float
//    }
}

