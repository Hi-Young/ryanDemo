package com.ryan.business.controller;

import com.ryan.business.entity.user.User;
import com.ryan.business.entity.user.UserChild;
import com.ryan.business.mapper.UserMapper;
import com.ryan.common.base.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MyBatis 拦截器测试控制器
 * 用于测试自动填充和查询安全检查功能
 */
@Slf4j
@RestController
@RequestMapping("/interceptor-test")
public class MyBatisInterceptorTestController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 测试插入 - 不传 name、createTime、updateTime
     * 拦截器会自动填充这些字段
     */
    @PostMapping("/insert-auto-fill")
    public ResultVO<String> testInsertAutoFill() {
        User user = new User();
        // 只设置必要字段，不设置 name、createTime、updateTime
        user.setEmail("test@example.com");
        user.setAge(25);
        user.setStatus(1);
        user.setId(5L);

        int result = userMapper.insert(user);
        log.info("插入结果: {}, 用户信息: {}", result, user);

        return ResultVO.success("插入成功，name、createTime、updateTime 已自动填充");
    }

    /**
     * 测试更新 - 不传 updateTime
     * 拦截器会自动填充 updateTime
     */
    @PutMapping("/update-auto-fill/{id}")
    public ResultVO<String> testUpdateAutoFill(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("updated@example.com");
        // 不设置 updateTime，拦截器会自动填充

        int result = userMapper.updateById(user);
        log.info("更新结果: {}, 用户信息: {}", result, user);

        return ResultVO.success("更新成功，updateTime 已自动填充");
    }

    /**
     * 测试安全查询 - 带 WHERE 条件
     * 这个查询会通过安全检查
     */
    @GetMapping("/safe-query-with-where/{id}")
    public ResultVO<User> testSafeQueryWithWhere(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        return ResultVO.success(user);
    }

    /**
     * 测试安全查询 - 带 LIMIT
     * 这个查询会通过安全检查
     */
    @GetMapping("/safe-query-with-limit")
    public ResultVO<List<UserChild>> testSafeQueryWithLimit() {
        // 注意：selectList 默认可能没有 WHERE 或 LIMIT
        // 需要使用自定义 SQL 或者 MyBatis Plus 的条件构造器
//        List<User> users = userMapper.selectList(null);
        List<UserChild> childList= userMapper.listAllDataPage(10);
        return ResultVO.success(childList);
    }

    /**
     * 测试不安全查询 - 既没有 WHERE 也没有 LIMIT
     * 这个查询会被拦截器拦截并抛出异常
     *
     * 注意：这个方法仅用于演示，实际使用时会抛出异常
     */
    @GetMapping("/unsafe-query")
    public ResultVO<List<User>> testUnsafeQuery() {
        try {
            // 这里需要执行一个不带 WHERE 和 LIMIT 的查询
            // 具体实现取决于你的 Mapper 中的自定义 SQL
            List<User> users = userMapper.selectList(null);
            return ResultVO.success(users);
        } catch (IllegalArgumentException e) {
            log.error("查询被拦截: {}", e.getMessage());
            return ResultVO.error("查询失败：" + e.getMessage());
        }
    }
}
