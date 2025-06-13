package com.geektime.concurrent.basic;

public class MemoryLeakExample {

    // 模拟一个不使用弱引用的 Entry 类
    static class Entry {
        // 如果这里是强引用，那么即使外部不再引用 ThreadLocal 对象，
        // 此处的 key 仍然会阻止 ThreadLocal 对象被垃圾回收，
        // 从而连同它关联的 value 也一直保留在内存中。
        ThreadLocal<?> key;  // 使用强引用
        Object value;

        Entry(ThreadLocal<?> key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    // 模拟线程内部的 ThreadLocalMap
    static class ThreadLocalMap {
        // 为简单起见，用一个 ArrayList 来存储 Entry 对象
        java.util.List<Entry> table = new java.util.ArrayList<>();

        // put 方法：将 (ThreadLocal, value) 键值对包装成 Entry 加入列表中
        void put(ThreadLocal<?> key, Object value) {
            table.add(new Entry(key, value));
        }

        int size() {
            return table.size();
        }
    }

    public static void main(String[] args) {
        ThreadLocalMap map = new ThreadLocalMap();

        // 模拟不断创建 ThreadLocal 对象并存入 map 中
        // 假设这些 ThreadLocal 对象在外部没有其他引用，应该被垃圾回收掉
        for (int i = 0; i < 10000; i++) {
            // 创建一个新的 ThreadLocal 对象
            ThreadLocal<String> local = new ThreadLocal<>();
            // 将 ThreadLocal 对象及其对应的值放入 map 中
            map.put(local, "Value " + i);
            // 此处 local 变量随循环结束就失去引用，
            // 但由于 map 中的 Entry 对象用的是强引用，
            // 这些 ThreadLocal 对象不会被回收，导致内存泄漏
        }

        // 打印 map 的大小，可以看到一直增长到 10000（或更多）
        // 这就是内存泄漏的体现，因为这些 Entry 永远不会被清理，
        // 如果积累太多，就可能引发内存溢出
        System.out.println("Map size (memory leak): " + map.size());
    }
}

