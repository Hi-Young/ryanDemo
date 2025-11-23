# 🎯 Day 3: 类型约束与泛型限制

## 今日目标

掌握两个核心能力：
1. **使用类型约束（Type Bounds）** - 限制泛型参数必须满足某些条件
2. **理解泛型限制（Type Erasure）** - 知道泛型不能做什么，以及如何绕过限制

---

## 为什么需要类型约束？

### 问题场景

```java
// ❌ 这段代码无法编译！
public static <T> T findMax(List<T> list) {
    T max = list.get(0);
    for (T item : list) {
        if (item.compareTo(max) > 0) {  // ❌ T没有compareTo方法！
            max = item;
        }
    }
    return max;
}
```

**原因**：编译器不知道 T 有 `compareTo` 方法。

**解决方案**：用类型约束告诉编译器 "T 必须实现 Comparable"

```java
// ✅ 加上约束后可以编译！
public static <T extends Comparable<T>> T findMax(List<T> list) {
    // 现在可以调用 compareTo 了
}
```

---

## 学习内容

### 1. before/ - 问题演示
运行 `TypeBoundsProblemDemo.java` 看看没有约束时的问题

### 2. practice/ - 填空练习
打开 `TypeBoundsFillInBlanks.java`，填写正确的类型约束

**练习内容：**
- ⭐ 练习1：实现通用排序（单一约束）
- ⭐⭐ 练习2：可比较且可序列化（多重约束）
- ⭐⭐ 练习3：数值运算（Number约束）
- ⭐⭐⭐ 练习4：复杂场景组合

### 3. limits/ - 泛型限制
了解类型擦除带来的限制，以及如何正确处理

---

## 核心知识点

### 类型约束语法

```java
// 1. 单一约束：必须实现某个接口
<T extends Comparable<T>>

// 2. 单一约束：必须继承某个类
<T extends Animal>

// 3. 多重约束：既要继承类，又要实现接口
<T extends Animal & Comparable<T> & Serializable>
// 注意：类必须写在最前面！

// 4. 通配符也可以有约束
List<? extends Number>
List<? super Integer>
```

### 判断技巧

**什么时候需要加约束？**

```
问：方法体内需要调用 T 的某个方法吗？
  ↓ 是（比如 compareTo、toString、equals 等特定方法）
答：需要加约束

问：只是存储、传递 T，不调用其特定方法？
  ↓ 是
答：不需要约束，用普通的 <T> 即可
```

### 类型擦除的限制

❌ **不能做的事：**
```java
new T()                    // 不能创建泛型对象
new T[10]                  // 不能创建泛型数组
T.class                    // 不能获取泛型的Class对象
instanceof T               // 不能用instanceof判断泛型类型
static T field             // 不能在静态上下文使用类型参数
```

✅ **解决方案：**
- 传入 `Class<T>` 对象
- 使用 `@SuppressWarnings("unchecked")`
- 用 `Object[]` 代替 `T[]`

---

## 练习流程

1. 先运行 `before/TypeBoundsProblemDemo.java` 看问题
2. 打开 `practice/TypeBoundsFillInBlanks.java` 做练习
3. 填写完成后运行 `main()` 验证
4. 对照 `practice/answer/TypeBoundsFillInBlanksAnswer.java` 检查答案
5. 阅读 `limits/TypeErasureLimitsDemo.java` 理解泛型限制

---

**准备好了吗？开始 Day 3 的学习！** 🚀
