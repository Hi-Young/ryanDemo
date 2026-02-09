package com.geektime.basic.generic.training.day1.refactor.after;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ 用泛型重构后的版本：GenericBox<T>
 *
 * 🎯 你的任务：
 * 实现这个泛型类，让它能够：
 * 1. 替代 UserBox、ProductBox、OrderBox 三个类
 * 2. 支持任意类型：GenericBox<User>、GenericBox<Product>、GenericBox<Order>
 * 3. 保持类型安全：GenericBox<User> 只能存 User，不能存 Product
 *
 * 💡 提示：
 * 1. 把所有的具体类型（User/Product/Order）替换成泛型参数 T
 * 2. 方法签名：public void add(T item)、public T get(int index) 等
 * 3. 内部存储：private List<T> items
 *
 * @param <T> 容器中存储的元素类型
 */
public class GenericBox<T> {

    // TODO: 定义存储结构
    // 提示：private List<T> items = new ArrayList<>();

    // TODO: 实现以下方法，参考 before/UserBox.java 的逻辑，但把 User 替换成 T

    /**
     * 添加元素
     */
    public void add(T item) {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 根据索引获取元素
     */
    public T get(int index) {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 移除元素
     */
    public boolean remove(T item) {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 获取所有元素
     */
    public List<T> getAll() {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 获取元素数量
     */
    public int size() {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 清空所有元素
     */
    public void clear() {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 判断是否包含某个元素
     */
    public boolean contains(T item) {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 获取第一个元素
     */
    public T getFirst() {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 获取最后一个元素
     */
    public T getLast() {
        // TODO: 实现
        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    public String toString() {
        // TODO: 实现
        return "GenericBox{" + "size=" + size() + ", items=" + getAll() + '}';
    }
}
