# MyBatis 拦截器使用说明

## 功能概述

`AutoFillAndQueryCheckInterceptor` 是一个 MyBatis 拦截器，提供以下功能：

### 1. 自动字段填充
- **INSERT 操作**：自动填充 `name`、`createTime`、`updateTime` 字段（仅当这些字段为空时）
- **UPDATE 操作**：自动填充 `updateTime` 字段（仅当该字段为空时）

### 2. 查询安全检查
- 所有 SELECT 查询必须包含 `WHERE` 或 `LIMIT` 子句
- 不符合条件的查询会抛出 `IllegalArgumentException` 异常

## 使用方式

### 自动填充示例

#### 插入数据
```java
@Autowired
private UserMapper userMapper;

// 不需要手动设置 name、createTime、updateTime
User user = new User();
user.setEmail("test@example.com");
user.setAge(25);
user.setStatus(1);

userMapper.insert(user);
// name 会被自动设置为 "system_auto"
// createTime 和 updateTime 会被自动设置为当前时间
```

#### 更新数据
```java
User user = new User();
user.setId(1L);
user.setEmail("updated@example.com");

userMapper.updateById(user);
// updateTime 会被自动设置为当前时间
```

### 查询安全检查

#### 安全的查询（会通过）
```java
// 带 WHERE 条件
User user = userMapper.selectById(1L);

// 使用条件构造器（自动生成 WHERE）
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("status", 1);
List<User> users = userMapper.selectList(wrapper);

// 带 LIMIT
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.last("LIMIT 10");
List<User> users = userMapper.selectList(wrapper);
```

#### 不安全的查询（会抛出异常）
```java
// 错误：既没有 WHERE 也没有 LIMIT
// 这会抛出 IllegalArgumentException
List<User> users = userMapper.selectList(null);
```

## 配置说明

拦截器已通过 `MyBatisConfig` 自动注册，无需额外配置。

如果需要自定义 `name` 字段的默认值，可以修改 `AutoFillAndQueryCheckInterceptor` 中的 `getDefaultName()` 方法：

```java
private String getDefaultName() {
    // 自定义默认值
    return "system_auto";
}
```

## 注意事项

1. **字段填充规则**：只有当字段值为 `null` 时才会自动填充，已有值不会被覆盖
2. **实体类要求**：实体类必须包含对应的字段（name、createTime、updateTime）
3. **查询安全**：所有查询语句都会被检查，建议使用 MyBatis Plus 的条件构造器来构建查询
4. **性能影响**：拦截器会对每个 SQL 操作进行拦截，对性能影响较小
5. **日志记录**：拦截器会记录详细的日志，便于调试和排查问题

## 测试接口

已提供测试控制器 `MyBatisInterceptorTestController`，包含以下测试接口：

- `POST /interceptor-test/insert-auto-fill` - 测试插入自动填充
- `PUT /interceptor-test/update-auto-fill/{id}` - 测试更新自动填充
- `GET /interceptor-test/safe-query-with-where/{id}` - 测试安全查询（带WHERE）
- `GET /interceptor-test/safe-query-with-limit` - 测试安全查询（带LIMIT）
- `GET /interceptor-test/unsafe-query` - 测试不安全查询（会抛出异常）

## 异常处理

当查询不符合安全规则时，会抛出以下异常：

```
IllegalArgumentException: 不安全的查询语句！必须包含 WHERE 或 LIMIT 子句。SQL: [Mapper方法全路径]
```

建议在全局异常处理器中捕获此异常，并返回友好的错误提示。

## 扩展建议

如果需要更灵活的控制，可以考虑以下扩展：

1. **跳过检查注解**：添加自定义注解，标记某些查询可以跳过安全检查
2. **自定义填充值**：支持通过配置文件或上下文获取填充值
3. **更细粒度的控制**：支持针对不同表或方法使用不同的填充策略
