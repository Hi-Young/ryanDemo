import com.geektime.designpattern.SinglePattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TestController {

    private static void test3_CopyList() {
//        System.out.println("【练习3】复制列表");
//        System.out.println("----------------------------------------");
//
//        List<Dog> dogs = Arrays.asList(new Dog("旺财"), new Dog("小黑"));
//        List<Animal> animals = new ArrayList<>();
//
//        copyList(dogs, animals);
//
//        System.out.println("✓ 从 List<Dog> 复制到 List<Animal>: " + animals);
//        System.out.println("✓ 测试通过！\n");
        List<Integer> integers = Arrays.asList(1, 2, 3);
        List<Number> numbers = new ArrayList<>();

        copyList(integers, numbers);
        System.out.println(numbers);
    }

    /**
     * 🎯 TODO 3: 填写两个通配符
     *
     * 需求：从源列表复制数据到目标列表
     * 分析：
     * - src（源列表）：需要**读取**数据
     * - dest（目标列表）：需要**写入**数据
     *
     * 问题1：src 应该填什么？
     * A. List<T>.
     *   
     * B. List<? extends T>
     * C. List<? super T>
     *
     * 问题2：dest 应该填什么？
     * A. List<T>
     * B. List<? extends T>
     * C. List<? super T>
     *
     * 答案：src=_____, dest=_____（在下面填写）
     */
    private static <T> void copyList(
            List<? extends T> src, List<T> dest) {
        // 方法体已实现
        for (T item : src) {
            dest.add(item);
        }
    }

    public static void main(String[] args) {
        ConcurrentHashMap<Long, Integer> map = new ConcurrentHashMap<>();
        Integer a = 100;
        map.put(1L, a);

        Integer stock = map.get(1L);
        stock += 1;

        System.out.println(stock);          // 101
        System.out.println(map.get(1L));    // 100  ← map里没变
        System.out.println(a);              // 100  ← a也没变
    }

    public <T> T createInstance(Class<T> clazz) throws Exception {
        return clazz.newInstance();
    }

    public void test() {
    }

    
}
