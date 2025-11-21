package com.geektime.basic.generic.training.day1.refactor.before;

import java.util.ArrayList;
import java.util.List;

public class GenericBox<T> {
    List<T> items = new ArrayList<>();;

    /**
     * 添加订单
     */
    public void add(T entity) {
        items.add(entity);
    }

    /**
     * 根据索引获取订单
     */
    public T get(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * 移除订单
     */
    public boolean remove(T entity) {
        return items.remove(entity);
    }

    /**
     * 获取所有订单
     */
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    /**
     * 获取订单数量
     */
    public int size() {
        return items.size();
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 清空所有订单
     */
    public void clear() {
        items.clear();
    }

    /**
     * 判断是否包含某个订单
     */
    public boolean contains(T entity) {
        return items.contains(entity);
    }

    /**
     * 获取第一个订单
     */
    public T getFirst() {
        return isEmpty() ? null : items.get(0);
    }

    /**
     * 获取最后一个订单
     */
    public T getLast() {
        return isEmpty() ? null : items.get(items.size() - 1);
    }
}
