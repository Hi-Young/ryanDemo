package com.geektime.basic.generic.training.day1.refactor.before;

import com.geektime.basic.generic.training.day1.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * ❌ 没有泛型的版本：UserBox（只能存User）
 *
 * 💔 痛点：
 * 1. 只能存User，不能复用
 * 2. 如果要存Product，必须再写一个ProductBox
 * 3. 如果要存Order，必须再写一个OrderBox
 * 4. 代码重复率90%！
 */
public class UserBox {

    private List<User> items = new ArrayList<>();

    /**
     * 添加用户
     */
    public void add(User user) {
        items.add(user);
    }

    /**
     * 根据索引获取用户
     */
    public User get(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * 移除用户
     */
    public boolean remove(User user) {
        return items.remove(user);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAll() {
        return new ArrayList<>(items);
    }

    /**
     * 获取用户数量
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
     * 清空所有用户
     */
    public void clear() {
        items.clear();
    }

    /**
     * 判断是否包含某个用户
     */
    public boolean contains(User user) {
        return items.contains(user);
    }

    /**
     * 获取第一个用户
     */
    public User getFirst() {
        return isEmpty() ? null : items.get(0);
    }

    /**
     * 获取最后一个用户
     */
    public User getLast() {
        return isEmpty() ? null : items.get(items.size() - 1);
    }

    @Override
    public String toString() {
        return "UserBox{" + "size=" + size() + ", items=" + items + '}';
    }
}
