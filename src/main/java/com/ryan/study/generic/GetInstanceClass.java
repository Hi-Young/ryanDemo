package com.ryan.study.generic;

import java.util.ArrayList;
import java.util.List;

public class GetInstanceClass<T> {

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
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
    }

    public T getInstance() {
        return null;
    }

}
