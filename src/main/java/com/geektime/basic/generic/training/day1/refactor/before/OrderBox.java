package com.geektime.basic.generic.training.day1.refactor.before;

import java.util.ArrayList;
import java.util.List;



/**
 * ❌ 没有泛型的版本：OrderBox（只能存Order）
 *
 * 💔 痛点：
 * 又是一模一样的代码！
 * 第三次复制粘贴了！
 * 如果有10个实体，就要写10个Box类！
 * 这就是泛型要解决的问题！
 */
public class OrderBox {

    private List<Order> items = new ArrayList<>();

    /**
     * 添加订单
     */
    public void add(Order order) {
        items.add(order);
    }

    /**
     * 根据索引获取订单
     */
    public Order get(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * 移除订单
     */
    public boolean remove(Order order) {
        return items.remove(order);
    }

    /**
     * 获取所有订单
     */
    public List<Order> getAll() {
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
    public boolean contains(Order order) {
        return items.contains(order);
    }

    /**
     * 获取第一个订单
     */
    public Order getFirst() {
        return isEmpty() ? null : items.get(0);
    }

    /**
     * 获取最后一个订单
     */
    public Order getLast() {
        return isEmpty() ? null : items.get(items.size() - 1);
    }

    @Override
    public String toString() {
        return "OrderBox{" + "size=" + size() + ", items=" + items + '}';
    }
}
