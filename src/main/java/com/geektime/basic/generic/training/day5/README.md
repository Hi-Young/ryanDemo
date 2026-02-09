# 🚀 Day 5: 高级泛型技巧

## 今日目标

掌握泛型的两个高级技巧：
1. **自限定类型（F-bounded Polymorphism）** - 让链式调用返回正确的子类类型
2. **TypeToken 模式** - 在运行时获取泛型类型信息

---

## 核心概念 1：自限定类型

### 什么是自限定类型？

一种"递归的类型约束"，类型参数约束自己：

```java
class Builder<T extends Builder<T>> {
              ↑                    ↑
              类型参数              约束自己
}
```

### 为什么需要它？

#### ❌ 没有自限定类型时的问题

```java
class Builder {
    public Builder setName(String name) {
        return this;  // 返回 Builder 类型
    }
}

class UserBuilder extends Builder {
    public UserBuilder setAge(int age) {
        return this;
    }
}

// 💔 链式调用断裂！
new UserBuilder()
    .setName("Tom")   // 返回 Builder 类型
    .setAge(18);      // ❌ 编译错误：Builder 没有 setAge 方法！
```

#### ✅ 使用自限定类型后

```java
class Builder<T extends Builder<T>> {
    public T setName(String name) {
        return (T) this;  // 返回子类类型
    }
}

class UserBuilder extends Builder<UserBuilder> {
                                  ↑
                          传入自己的类型！
    public UserBuilder setAge(int age) {
        return this;
    }
}

// ✅ 链式调用完美！
new UserBuilder()
    .setName("Tom")   // 返回 UserBuilder 类型
    .setAge(18);      // ✓ 可以继续调用
```

---

## 核心概念 2：TypeToken 模式

### 什么是 TypeToken？

一种在运行时获取泛型类型信息的技巧，Gson、Guava 等库都在使用。

### 为什么需要它？

#### ❌ 类型擦除导致的问题

```java
// 想反序列化为 List<User>
String json = "[{\"name\":\"Tom\"}, {\"name\":\"Jerry\"}]";

// ❌ 方式1：无法传递泛型信息
List<User> users = fromJson(json, List.class);  // 只知道是 List，不知道元素是 User

// ❌ 方式2：Class<T> 也无法表示复杂类型
List<User> users = fromJson(json, List<User>.class);  // 语法错误！
```

#### ✅ TypeToken 解决方案

```java
// Gson 的用法
Type type = new TypeToken<List<User>>(){}.getType();
List<User> users = gson.fromJson(json, type);  // ✓ 完整的类型信息
```

**原理**：通过匿名内部类捕获泛型信息。

---

## 学习内容

### 1. before/ - 问题演示
- `ChainCallProblem.java` - 链式调用返回类型问题
- `TypeErasureProblem.java` - 类型擦除带来的问题

### 2. after/ - 解决方案
- `SelfBoundedTypeDemo.java` - 自限定类型解决链式调用
- `TypeTokenDemo.java` - TypeToken 获取运行时类型

### 3. practice/ - 填空练习
- `SelfBoundedFillInBlanks.java` - 自限定类型练习
- `TypeTokenFillInBlanks.java` - TypeToken 实现练习

### 4. advanced/ - 高级应用
- `RealWorldExamples.java` - 实际项目中的应用
- `BridgeMethodDemo.java` - 桥接方法演示

---

## 核心知识点

### 自限定类型的语法

```java
// 基础模式
class Base<T extends Base<T>> {
    public T doSomething() {
        return (T) this;
    }
}

class Derived extends Base<Derived> {
              继承时"传入自己"
}
```

### 理解技巧

```
问：为什么要写 <T extends Base<T>>？
答：让每个子类的方法都返回自己的类型，而不是父类类型

问：为什么子类要写 extends Base<Derived>？
答：告诉编译器"T 就是 Derived"，这样父类方法返回的 T 就是 Derived
```

### TypeToken 的实现原理

```java
public abstract class TypeToken<T> {
    private final Type type;

    protected TypeToken() {
        // 通过反射获取子类的泛型参数
        Type superclass = getClass().getGenericSuperclass();
        ParameterizedType parameterized = (ParameterizedType) superclass;
        this.type = parameterized.getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}

// 使用时创建匿名子类
TypeToken<List<String>> token = new TypeToken<List<String>>(){};
//                                                           ↑↑
//                                                      匿名内部类
```

---

## 应用场景

### 自限定类型的应用

1. **Builder 模式** - Lombok 的 `@Builder` 底层原理
2. **Fluent API** - 链式调用
3. **实体类继承** - 让 CRUD 方法返回正确的子类型
4. **Enum 枚举** - `Enum<E extends Enum<E>>` 就是自限定类型

### TypeToken 的应用

1. **JSON 序列化** - Gson、Jackson
2. **依赖注入** - Guice 使用 TypeToken 绑定类型
3. **泛型数组创建** - 绕过类型擦除
4. **框架开发** - 获取用户定义的泛型参数

---

## 练习流程

1. **理解问题** → 运行 `before/` 目录下的文件
2. **看解决方案** → 运行 `after/` 目录下的文件
3. **动手练习** → 填写 `practice/` 目录下的空白
4. **高级应用** → 阅读 `advanced/` 目录的实战案例

---

## 难度评估

- ⭐⭐⭐ 自限定类型 - 需要转变思维方式
- ⭐⭐⭐ TypeToken - 涉及反射和匿名内部类
- ⭐⭐⭐⭐ 桥接方法 - 理解类型擦除的底层机制

**建议**：先专注于自限定类型，TypeToken 可以在需要时再深入。

---

**准备好挑战泛型的终极技巧了吗？** 🔥
