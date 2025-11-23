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

## Day 3: 类型约束 + 泛型限制 💡

### 核心知识点
- 类型约束 `<T extends Comparable<T>>`
- 多重边界 `<T extends Number & Comparable<T>>`
- 类型擦除原理与限制

### 实战场景
**问题**：
- 想实现通用的 findMax，但 `<T>` 太宽泛，不知道 T 有 compareTo 方法
- 想对不同数字类型求和，不想写重复代码
- 需要理解泛型的限制（不能 new T()、不能 new T[]）

**解决方案**：使用类型约束告诉编译器"T必须满足某些条件"。

### 练习任务（填空练习）
1. ⭐ 填写单一约束：`findMax`、`sum`、`sortList`
2. ⭐⭐ 填写多重约束：可比较且可序列化
3. ⭐⭐⭐ 综合练习：约束 + 通配符组合

### 学习路径
1. 运行 `before/TypeBoundsProblemDemo.java` 看问题
2. 运行 `after/TypeBoundsSolutionDemo.java` 看解决方案
3. 打开 `practice/TypeBoundsFillInBlanks.java` 做填空练习
4. 阅读 `limits/TypeErasureLimitsDemo.java` 理解泛型限制

**目标**：
- 理解什么时候需要类型约束
- 掌握单一约束和多重约束语法
- 理解类型擦除带来的限制及解决方案

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

## Day 5: 高级技巧 (自限定类型 + TypeToken) 🚀

### 核心知识点
- **自限定类型**（F-bounded Polymorphism）：`<T extends Builder<T>>`
- **TypeToken 模式**：运行时获取泛型类型信息
- 理解类型擦除的底层机制

### 实战场景

**问题1：链式调用类型丢失**
- Builder 模式继承时，父类方法返回 `Builder`，无法继续调用子类方法
- 实体类的链式 setter 无法返回正确的子类类型

**问题2：运行时类型信息丢失**
- 无法创建泛型数组 `new T[size]`
- JSON 反序列化时无法知道 `List<User>` 的元素类型

**解决方案：**
- 自限定类型：让父类方法返回子类类型
- TypeToken：通过匿名内部类捕获泛型信息

### 练习任务（填空练习）

**自限定类型：**
1. ⭐⭐ Builder 模式和实体类链式调用
2. ⭐⭐⭐ Fluent API（Query DSL）

**TypeToken：**
1. ⭐⭐ 实现 TypeToken 的核心逻辑
2. ⭐⭐ 处理复杂泛型类型（`List<User>`）
3. ⭐⭐⭐ 模拟 Gson 的用法

### 学习路径
1. 运行 `before/ChainCallProblem.java` 和 `TypeErasureProblem.java` 看问题
2. 运行 `after/SelfBoundedTypeDemo.java` 和 `TypeTokenDemo.java` 看解决方案
3. 完成 `practice/` 目录下的填空练习
4. 阅读 `advanced/RealWorldExamples.java` 看真实应用

**真实应用：**
- Lombok @Builder 的底层原理
- JPA 实体类的链式 setter
- MyBatis-Plus 的 QueryWrapper
- Java Enum 的 `Enum<E extends Enum<E>>`
- Gson 的 TypeToken

**目标**：
- 理解自限定类型的核心模式
- 掌握 TypeToken 获取运行时类型的技巧
- 认识泛型在主流框架中的高级应用

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
