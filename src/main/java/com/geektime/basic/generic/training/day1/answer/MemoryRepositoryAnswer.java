package com.geektime.basic.generic.training.day1.answer;

import com.geektime.basic.generic.training.day1.BaseRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参考答案：基于内存的Repository实现
 *
 * ⚠️ 先自己完成练习，再来看这个答案！
 */
public abstract class MemoryRepositoryAnswer<T, ID> implements BaseRepository<T, ID> {

    protected final Map<ID, T> storage = new ConcurrentHashMap<>();

    protected abstract ID getId(T entity);
    protected abstract void setId(T entity, ID id);
    protected abstract ID generateId();

    @Override
    public T save(T entity) {
        ID id = getId(entity);
        if (id == null) {
            // 生成新ID
            id = generateId();
            setId(entity, id);
        }
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(ID id) {
        return storage.remove(id) != null;
    }

    @Override
    public T update(T entity) {
        ID id = getId(entity);
        if (id == null || !storage.containsKey(id)) {
            throw new IllegalArgumentException("实体不存在，无法更新: " + id);
        }
        storage.put(id, entity);
        return entity;
    }

    @Override
    public long count() {
        return storage.size();
    }
}
