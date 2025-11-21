# 🎯 Java泛型实战训练营

## 学习目标
通过5天的实战练习，从"知道泛型"到"会用泛型"，掌握在真实业务场景中运用泛型的能力。

## 训练营规则
1. **每天一个主题**，包含理论 + 实战练习
2. **完成练习后**运行测试验证，通过后进入下一关
3. **边写边思考**：为什么这里要用泛型？不用会怎样？
4. **实战导向**：所有练习都来自真实项目场景

---

## Day 1: 泛型基础 + 通用DAO层 🌟

### 核心知识点
- 泛型类、泛型方法、泛型接口
- 类型参数命名规范 (T, E, K, V, R)
- 泛型的编译时类型安全

### 实战场景
**问题**：你的项目有多个实体类（User、Order、Product），每个都需要写一套增删改查方法，代码重复严重。

**解决方案**：用泛型实现一个通用的BaseDAO<T>，一次编写，处处复用。

### 练习任务
1. 实现 `BaseRepository<T>` 通用数据访问接口
2. 实现 `BaseService<T, ID>` 通用业务服务层
3. 创建具体的 `UserRepository` 和 `ProductRepository`

**目标**：减少70%的重复代码，理解"泛型消除重复"的威力。

---

## Day 2: 通配符实战 (? extends / ? super) 🔥

### 核心知识点
- 上界通配符 `? extends T` (生产者Producer)
- 下界通配符 `? super T` (消费者Consumer)
- PECS原则 (Producer Extends, Consumer Super)

### 实战场景
**问题**：如何实现一个能处理"用户及其所有子类"的通用数据转换器？

**解决方案**：掌握通配符的"协变"和"逆变"能力。

### 练习任务
1. 实现 `DataConverter<S, T>` 数据转换器
2. 实现 `copyList(List<? extends T> source)` 方法
3. 实现 `addAll(List<? super T> dest, List<? extends T> src)` 方法

**目标**：理解PECS原则，知道什么时候用extends，什么时候用super。

---

## Day 3: 泛型约束 + 类型推断 💡

### 核心知识点
- 泛型约束 `<T extends Comparable<T>>`
- 多重边界 `<T extends A & B & C>`
- 类型推断和菱形操作符

### 实战场景
**问题**：实现一个通用的排序工具，要求T必须可比较。

**解决方案**：使用泛型约束限定类型参数的能力。

### 练习任务
1. 实现 `SortUtil.sort(List<T extends Comparable<T>>)` 排序工具
2. 实现 `Pair<K extends Comparable<K>, V>` 可排序键值对
3. 实现 `JsonSerializer<T extends Serializable & Cloneable>` 多重约束

**目标**：理解如何约束泛型的能力边界。

---

## Day 4: 泛型 + 设计模式组合 🎨

### 核心知识点
- 泛型 + 策略模式
- 泛型 + 构建器模式
- 泛型 + 工厂模式

### 实战场景
**问题**：你的促销系统有多种策略，如何用泛型让策略模式更优雅？

**解决方案**：结合设计模式，发挥泛型的最大价值。

### 练习任务
1. 改造现有的 `PromotionStrategy` 支持泛型输入输出
2. 实现 `Builder<T>` 通用构建器
3. 实现 `Factory<T>` 通用工厂

**目标**：在设计模式中自如运用泛型。

---

## Day 5: 高级场景 (自限定类型、类型擦除) 🚀

### 核心知识点
- 递归类型限定 `<T extends Comparable<T>>`
- 自限定类型 `<T extends BaseEntity<T>>`
- 类型擦除原理和桥接方法
- 获取泛型类型信息 (TypeToken)

### 实战场景
**问题**：实现Fluent API，让链式调用返回正确的子类类型。

**解决方案**：使用自限定泛型（F-bounded polymorphism）。

### 练习任务
1. 实现 `BaseBuilder<T extends BaseBuilder<T>>` 支持链式调用
2. 实现 `TypeToken<T>` 获取运行时泛型信息
3. 实现 `JsonUtil.fromJson(String json, Class<T> clazz)` 类型安全的反序列化

**目标**：掌握泛型的高级技巧，理解类型擦除的本质。

---

## 🏆 评估标准

每天完成练习后，问自己3个问题：
1. ✅ 如果不用泛型，代码会怎样？
2. ✅ 这个泛型解决了什么问题？
3. ✅ 我能举一反三，应用到自己项目中吗？

## 🎓 结业成果

完成5天训练后，你将能够：
- 独立设计泛型类和泛型方法
- 在DAO层、Service层熟练使用泛型
- 理解Spring、MyBatis等框架中的泛型设计
- 在设计模式中灵活运用泛型
- 读懂Java集合框架的泛型源码

---

**准备好了吗？让我们从Day 1开始！** 🚀
