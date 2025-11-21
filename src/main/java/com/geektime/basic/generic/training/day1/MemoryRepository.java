package com.geektime.basic.generic.training.day1;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 基于内存的Repository实现（模拟数据库操作）
 *
 * 🎯 练习任务：完成下面的TODO部分
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
@Slf4j
public abstract class MemoryRepository<T, ID> implements BaseRepository<T, ID> {

    // 使用ConcurrentHashMap模拟数据库存储
    protected final Map<ID, T> storage = new ConcurrentHashMap<>();

    /**
     * 获取实体的ID
     * 子类需要实现这个方法来告诉父类如何获取ID
     */
    protected abstract ID getId(T entity);

    /**
     * 设置实体的ID
     * 子类需要实现这个方法来告诉父类如何设置ID
     */
    protected abstract void setId(T entity, ID id);
    
    protected abstract ID generateId();

    @Override
    public T save(T entity) {
        // TODO: 实现保存逻辑
        // 1. 如果entity的ID为null，生成一个新ID
        // 2. 将entity存入storage
        // 3. 返回保存后的entity
        
        ID id = getId(entity);
        if(Objects.isNull(id)) {
            id = generateId();
            setId(entity, id);
        }
        storage.put(getId(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        // TODO: 实现查找逻辑
        // 1. 从storage中获取entity
        // 2. 使用Optional.ofNullable包装结果
        T entity = storage.get(id);
        return Optional.ofNullable(entity);
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
        // TODO: 实现查找所有逻辑
        // 返回storage中的所有值
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    public boolean deleteById(ID id) {
        T removedId = storage.remove(id);
        return Objects.nonNull(removedId);
        // TODO: 实现删除逻辑
        // 1. 尝试从storage中移除
        // 2. 返回是否删除成功
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    public T update(T entity) {
        ID id = getId(entity);
        boolean b = storage.containsKey(id);
        if (b) {
            storage.put(id, entity);
            return entity;
        } else {
            throw new IllegalArgumentException("实体不存在");
        } 
        // TODO: 实现更新逻辑
        // 1. 获取entity的ID
        // 2. 检查storage中是否存在该ID
        // 3. 如果存在则更新，否则抛出异常
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    public long count() {
        return storage.size();
        // TODO: 实现统计逻辑
        // 返回storage的大小
//        throw new UnsupportedOperationException("请实现这个方法");
    }
}
