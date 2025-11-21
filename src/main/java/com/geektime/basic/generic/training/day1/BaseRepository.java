package com.geektime.basic.generic.training.day1;

import java.util.List;
import java.util.Optional;

/**
 * Day 1 练习：通用数据访问层接口
 *
 * 🎯 目标：实现一个泛型Repository接口，让所有实体的DAO都能复用这些方法
 *
 * 💡 思考：
 * 1. 为什么需要两个泛型参数 T 和 ID？
 * 2. 如果不用泛型，UserRepository 和 ProductRepository 会有多少重复代码？
 * 3. Optional<T> 是如何利用泛型的？
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
public interface BaseRepository<T, ID> {

    /**
     * 保存实体
     * TODO: 实现这个方法，返回保存后的实体
     */
    T save(T entity);

    /**
     * 根据ID查找实体
     * TODO: 实现这个方法，返回Optional包装的实体
     * 提示：使用Optional.ofNullable()
     */
    Optional<T> findById(ID id);

    /**
     * 查找所有实体
     * TODO: 实现这个方法，返回实体列表
     */
    List<T> findAll();

    /**
     * 根据ID删除实体
     * TODO: 实现这个方法，返回是否删除成功
     */
    boolean deleteById(ID id);

    /**
     * 更新实体
     * TODO: 实现这个方法，返回更新后的实体
     */
    T update(T entity) throws Exception;

    /**
     * 统计实体总数
     * TODO: 实现这个方法，返回实体数量
     */
    long count();
}