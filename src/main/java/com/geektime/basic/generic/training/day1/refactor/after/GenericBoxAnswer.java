package com.geektime.basic.generic.training.day1.refactor.after;

import java.util.ArrayList;
import java.util.List;

/**
 * 参考答案：GenericBox<T>
 *
 * ⚠️ 先自己实现，实在卡住了再看这个答案！
 */
public class GenericBoxAnswer<T> {

    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
    }

    public T get(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    public boolean remove(T item) {
        return items.remove(item);
    }

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public boolean contains(T item) {
        return items.contains(item);
    }

    public T getFirst() {
        return isEmpty() ? null : items.get(0);
    }

    public T getLast() {
        return isEmpty() ? null : items.get(items.size() - 1);
    }

    @Override
    public String toString() {
        return "GenericBox{" + "size=" + size() + ", items=" + items + '}';
    }
}
