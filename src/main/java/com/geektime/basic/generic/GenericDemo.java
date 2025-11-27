package com.geektime.basic.generic;

import java.util.*;
import java.io.Serializable;

/**
 * 泛型完整示例 - 涵盖工作和框架常见用法
 */
public class GenericDemo {

    // ==================== 1. 类上声明泛型 ====================
    // 泛型在类级别声明，整个类的实例方法都能用这个T
    static class Box<T> {
        private T data;
        
        // 构造方法可以用类泛型
        public Box(T data) {
            this.data = data;
        }
        
        // 实例方法可以用类泛型
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
        
        // 实例方法还可以声明自己的泛型（和类泛型T不冲突）
        public <R> R transform(Function<T, R> func) {
            return func.apply(data);
        }
    }
    
    // 使用示例
    public static void testClassGeneric() {
        Box<String> stringBox = new Box<>("hello");
        String data = stringBox.getData();  // 不需要强转
        
        // transform方法的R是方法泛型，和类的T独立
        Integer length = stringBox.transform(s -> s.length());
    }

    // ==================== 2. 方法上声明泛型 ====================
    // 方法泛型：在返回值前声明 <T>，只在这个方法内有效
    public static <T> T getFirst(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }
    
    // 方法泛型可以有多个
    public static <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
    
    // 方法泛型可以有边界限定
    public static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) > 0 ? a : b;
    }
    
    public static void testMethodGeneric() {
        List<String> list = Arrays.asList("a", "b", "c");
        String first = getFirst(list);  // 编译器推断T=String
        
        Map<String, Integer> map = createMap("age", 18);  // 推断K=String, V=Integer
        
        Integer maxNum = max(10, 20);  // T必须实现Comparable
    }

    // ==================== 3. 通配符 ? ====================
    
    // 3.1 无界通配符 <?> - 只能读不能写
    public static void printList(List<?> list) {
        for (Object item : list) {  // 只能读成Object
            System.out.println(item);
        }
        // list.add("x");  // ❌ 编译错误：不知道?是什么类型
        // list.add(null);  // ✅ null例外，可以加
    }
    
    // 3.2 上界通配符 <? extends T> - 读取时有类型保证
    public static double sumNumbers(List<? extends Number> list) {
        double sum = 0;
        for (Number num : list) {  // 能读成Number
            sum += num.doubleValue();
        }
        // list.add(1);  // ❌ 不能写入
        return sum;
    }
    
    public static void testExtendsWildcard() {
        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Double> doubles = Arrays.asList(1.1, 2.2);
        
        sumNumbers(ints);     // ✅ Integer extends Number
        sumNumbers(doubles);  // ✅ Double extends Number
    }
    
    // 3.3 下界通配符 <? super T> - 可以写入T及其子类
    public static void addNumbers(List<? super Integer> list) {
        list.add(1);        // ✅ 可以写入Integer
        list.add(100);      // ✅ 可以写入Integer
        // list.add(1.5);   // ❌ Double不是Integer子类
        
        Object obj = list.get(0);  // 读取只能是Object
    }
    
    public static void testSuperWildcard() {
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        
        addNumbers(numbers);  // ✅ Number是Integer的父类
        addNumbers(objects);  // ✅ Object是Integer的父类
    }

    // ==================== 4. 泛型传递（常见框架写法）====================
    
    // 4.1 Builder模式中的泛型传递
    static class Response<T> {
        private int code;
        private String message;
        private T data;
        
        public static <T> ResponseBuilder<T> builder() {
            return new ResponseBuilder<>();
        }
        
        static class ResponseBuilder<T> {  // Builder也要声明泛型
            private int code;
            private String message;
            private T data;
            
            public ResponseBuilder<T> code(int code) {
                this.code = code;
                return this;  // 链式调用
            }
            
            public ResponseBuilder<T> message(String message) {
                this.message = message;
                return this;
            }
            
            public ResponseBuilder<T> data(T data) {  // 泛型传递
                this.data = data;
                return this;
            }
            
            public Response<T> build() {
                Response<T> response = new Response<>();
                response.code = this.code;
                response.message = this.message;
                response.data = this.data;
                return response;
            }
        }
    }
    
    public static void testGenericTransfer() {
        Response<String> response = Response.<String>builder()
                .code(200)
                .message("success")
                .data("hello")
                .build();
    }
    
    // 4.2 DAO层常见泛型传递
    interface BaseDao<T> {
        T findById(Long id);
        List<T> findAll();
        void save(T entity);
    }
    
    static class UserDao implements BaseDao<User> {  // 传递具体类型
        @Override
        public User findById(Long id) {
            // 查询逻辑
            return null;
        }
        
        @Override
        public List<User> findAll() {
            return null;
        }
        
        @Override
        public void save(User entity) {
            // 保存逻辑
        }
    }
    
    // 4.3 通用Service层（保持泛型传递）
    static class BaseService<T, D extends BaseDao<T>> {  // D也要传递T
        protected D dao;
        
        public BaseService(D dao) {
            this.dao = dao;
        }
        
        public T getById(Long id) {
            return dao.findById(id);
        }
        
        public List<T> listAll() {
            return dao.findAll();
        }
    }
    
    static class UserService extends BaseService<User, UserDao> {
        public UserService(UserDao dao) {
            super(dao);
        }
        
        // 可以添加特有方法
        public User findByUsername(String username) {
            return null;
        }
    }

    // ==================== 5. 多重边界限定 ====================
    
    // T必须同时满足多个条件（类在前，接口在后，用&连接）
//    public static <T extends Animal & Comparable<T>> T findMax(List<T> list) {
//        if (list.isEmpty()) return null;
//        T max = list.get(0);
//        for (T item : list) {
//            if (item.compareTo(max) > 0) {
//                max = item;
//            }
//        }
//        return max;
//    }

    // ==================== 6. 框架工具类常见写法 ====================
    
    // 6.1 类型转换工具（类似BeanUtils）
    static class BeanUtil {
        public static <S, T> T convert(S source, Class<T> targetClass) {
            // 实际会用反射复制属性
            try {
                T target = targetClass.newInstance();
                // ... 复制属性逻辑
                return target;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
            List<T> result = new ArrayList<>();
            for (S source : sourceList) {
                result.add(convert(source, targetClass));
            }
            return result;
        }
    }
    
    // 6.2 结果包装类（类似EasyExcel的写法）
    static class Result<T> {
        private boolean success;
        private T data;
        private String errorMsg;
        
        public static <T> Result<T> ok(T data) {
            Result<T> result = new Result<>();
            result.success = true;
            result.data = data;
            return result;
        }
        
        public static <T> Result<T> fail(String errorMsg) {
            Result<T> result = new Result<>();
            result.success = false;
            result.errorMsg = errorMsg;
            return result;
        }
    }
    
    // 6.3 分页结果（常见业务场景）
    static class PageResult<T> {
        private List<T> records;
        private long total;
        private int pageSize;
        private int currentPage;
        
        // 类型转换：把PageResult<A>转成PageResult<B>
        public <R> PageResult<R> convert(Function<T, R> mapper) {
            PageResult<R> result = new PageResult<>();
            result.total = this.total;
            result.pageSize = this.pageSize;
            result.currentPage = this.currentPage;
            
            result.records = new ArrayList<>();
            for (T record : this.records) {
                result.records.add(mapper.apply(record));
            }
            return result;
        }
    }
    
    public static void testPageResultConvert() {
        PageResult<User> userPage = new PageResult<>();
        // 转成DTO
        PageResult<UserDTO> dtoPage = userPage.convert(user -> {
            UserDTO dto = new UserDTO();
            dto.name = user.name;
            return dto;
        });
    }

    // ==================== 7. PECS原则（重要！）====================
    // Producer Extends, Consumer Super
    
    // 7.1 生产者 - 从集合里读数据（用extends）
    public static void copyProducer(List<? extends Number> src, List<Number> dest) {
        for (Number num : src) {  // 读取数据：生产者
            dest.add(num);
        }
    }
    
    // 7.2 消费者 - 往集合里写数据（用super）
    public static void copyConsumer(List<Integer> src, List<? super Integer> dest) {
        for (Integer num : src) {
            dest.add(num);  // 写入数据：消费者
        }
    }
    
    public static void testPECS() {
        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Number> numbers = new ArrayList<>();
        
        copyProducer(ints, numbers);  // ints是生产者（读），用extends
        
        List<Object> objects = new ArrayList<>();
        copyConsumer(ints, objects);  // objects是消费者（写），用super
    }

    // ==================== 8. 泛型数组的坑 ====================
    
    public static void testGenericArray() {
        // List<String>[] arr = new List<String>[10];  // ❌ 编译错误
        List<?>[] arr = new List<?>[10];  // ✅ 通配符可以
        
        // 或者用集合的集合
        List<List<String>> listOfList = new ArrayList<>();
    }

    // ==================== 9. 类型擦除后的桥接方法 ====================
    
    interface Processor<T> {
        void process(T data);
    }
    
    static class StringProcessor implements Processor<String> {
        @Override
        public void process(String data) {  // 看起来只有这一个方法
            System.out.println(data);
        }
        
        // 但编译器会生成桥接方法：
        // public void process(Object data) {
        //     process((String) data);
        // }
    }

    // ==================== 10. 常见的坑和注意事项 ====================
    
    public static void commonMistakes() {
        // 坑1：不能用基本类型
        // List<int> list;  // ❌ 错误
        List<Integer> list = new ArrayList<>();  // ✅ 正确
        
        // 坑2：不能直接创建泛型数组
        // T[] arr = new T[10];  // ❌ 错误
        // 解决方案：
        Object[] arr = new Object[10];
        
        // 坑3：静态方法/字段不能用类泛型
        // static class Wrong<T> {
        //     static T data;  // ❌ 静态字段不能用T
        //     static T getData() { return null; }  // ❌ 静态方法不能用T
        // }
        
        // 坑4：instanceof不能用泛型类型
        Object obj = "hello";
        // if (obj instanceof List<String>) { }  // ❌ 错误
        if (obj instanceof List<?>) { }  // ✅ 只能用通配符
    }

    // ==================== 辅助类 ====================
    
    static class User {
        String name;
        int age;
    }
    
    static class UserDTO {
        String name;
    }
    
    static abstract class Animal implements Comparable<Animal> {
        abstract String getName();
    }
    
    interface Function<T, R> {
        R apply(T t);
    }
}