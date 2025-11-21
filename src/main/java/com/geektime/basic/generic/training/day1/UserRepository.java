package com.geektime.basic.generic.training.day1;

import com.geektime.basic.generic.training.day1.entities.User;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户Repository实现
 *
 * 🎯 练习任务：继承MemoryRepository，指定正确的泛型参数
 *
 * 💡 思考：
 * 1. 为什么这里要写 <User, Long> 而不是 <T, ID>？
 * 2. 继承泛型类时，泛型参数是如何传递的？
 */
public class UserRepository extends MemoryRepository<User, Long> {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    protected Long getId(User entity) {
        Long id = entity.getId();
        return id;
        // TODO: 返回用户的ID
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    protected void setId(User entity, Long id) {
        entity.setId(id);
        // TODO: 设置用户的ID
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 生成新的用户ID
     */
    protected Long generateId() {
        return idGenerator.getAndIncrement();
    }

    // 🎯 扩展练习：添加一个特有的方法
    // TODO: 实现根据用户名查找用户
    public User findByUsername(String username) {
//        throw new UnsupportedOperationException("请实现这个方法");
        return storage.values().stream().filter(item -> item.getUsername().equals(username)).findFirst().get();
        
        
    }
}
