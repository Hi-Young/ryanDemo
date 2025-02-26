import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GenericTest {

    // 1. 只能读取的List (生产者)
    public void printList(List<? extends Number> list) {
        // 可以读取数据
        Number number = list.get(0);
        System.out.println(number);
        // 但不能添加数据(null除外)
//        list.add(1); // 编译错
    }

    // 2. 只能写入的List (消费者)
    public void addToList(List<? super Integer> list) {
        // 可以添加数据
        list.add(1);
        // 读取出来的只能是Object
        Object obj = list.get(0);
    }

    // 3. 既能读又能写，但类型完全不确定
    public void process(List<?> list) {
        // 只能读出Object
        Object obj = list.get(0);
        // 只能添加null
        list.add(null);
    }

    @Test
    public void test1() {

        List<Integer> list = Arrays.asList(1, 3, 5, 1);
        printList(list);

        System.out.println(0.2<0.3);
    }
}
