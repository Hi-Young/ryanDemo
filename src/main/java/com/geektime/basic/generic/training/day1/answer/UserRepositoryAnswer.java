package com.geektime.basic.generic.training.day1.answer;

import com.geektime.basic.generic.training.day1.entities.User;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 参考答案：用户Repository实现
 *
 * ⚠️ 先自己完成练习，再来看这个答案！
 */
public class UserRepositoryAnswer extends MemoryRepositoryAnswer<User, Long> {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    protected Long getId(User entity) {
        return entity.getId();
    }

    @Override
    protected void setId(User entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long generateId() {
        return idGenerator.getAndIncrement();
    }

    public User findByUsername(String username) {
        return storage.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
