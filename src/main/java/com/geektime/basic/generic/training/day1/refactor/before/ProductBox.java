package com.geektime.basic.generic.training.day1.refactor.before;

import com.geektime.basic.generic.training.day1.entities.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * ❌ 没有泛型的版本：ProductBox（只能存Product）
 *
 * 💔 痛点：
 * 看到了吗？这个类和 UserBox 几乎一模一样！
 * 只是把 User 换成了 Product！
 * 这就是没有泛型的痛苦！
 */
public class ProductBox {

    private List<Product> items = new ArrayList<>();

    /**
     * 添加商品
     */
    public void add(Product product) {
        items.add(product);
    }

    /**
     * 根据索引获取商品
     */
    public Product get(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * 移除商品
     */
    public boolean remove(Product product) {
        return items.remove(product);
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAll() {
        return new ArrayList<>(items);
    }

    /**
     * 获取商品数量
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
     * 清空所有商品
     */
    public void clear() {
        items.clear();
    }

    /**
     * 判断是否包含某个商品
     */
    public boolean contains(Product product) {
        return items.contains(product);
    }

    /**
     * 获取第一个商品
     */
    public Product getFirst() {
        return isEmpty() ? null : items.get(0);
    }

    /**
     * 获取最后一个商品
     */
    public Product getLast() {
        return isEmpty() ? null : items.get(items.size() - 1);
    }

    @Override
    public String toString() {
        return "ProductBox{" + "size=" + size() + ", items=" + items + '}';
    }
}
