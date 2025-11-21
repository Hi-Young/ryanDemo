# 🚀 泛型训练营快速开始

## 如何使用这个训练营

### 第一步：阅读学习路线
打开 `README.md`，了解整个训练营的安排。

### 第二步：开始 Day 1 练习

#### 1. 找到练习文件
```
training/day1/
├── BaseRepository.java          # 接口定义（已完成）
├── MemoryRepository.java        # 待完成 ⭐️
├── UserRepository.java          # 待完成 ⭐️
├── ProductRepository.java       # 待完成 ⭐️
├── Day1Test.java                # 测试类（已完成）
└── entities/
    ├── User.java                # 实体类（已完成）
    └── Product.java             # 实体类（已完成）
```

#### 2. 完成 TODO 部分
在 IntelliJ IDEA 中：
- 打开 `MemoryRepository.java`
- 查找所有的 `TODO` 注释
- 实现每个方法，替换 `throw new UnsupportedOperationException()`

#### 3. 运行测试验证
```bash
# 方式1：在IDE中
右键 Day1Test.java -> Run 'Day1Test.main()'

# 方式2：使用Maven
mvn test -Dtest=Day1Test
```

#### 4. 对照参考答案
完成练习后，查看 `day1/answer/` 目录下的参考答案。

#### 5. 反思总结
回答 Day1Test 结尾处的3个思考题：
1. 如果没有泛型，会有多少重复代码？
2. 泛型如何实现"一次编写，处处复用"？
3. 为什么需要两个类型参数 T 和 ID？

---

## 💡 学习技巧

### 技巧1：带着问题学习
每完成一个方法，问自己：
- 这里为什么要用泛型 T？
- 如果把 T 换成具体类型（如 User），会怎样？
- 泛型给我带来了什么好处？

### 技巧2：对比学习
观察 `UserRepository<User, Long>` 和 `ProductRepository<Product, String>`：
- 它们有什么共同点？
- 它们有什么不同点？
- 泛型如何让它们共享代码？

### 技巧3：举一反三
完成练习后，尝试：
- 新增一个 `Order` 实体
- 创建一个 `OrderRepository<Order, UUID>`
- 验证你的泛型设计是否真的通用

### 技巧4：阅读注释
每个文件都有详细的注释，包括：
- 🎯 练习目标
- 💡 思考问题
- ⚠️ 注意事项

---

## ✅ 验收标准

完成 Day 1 后，你应该能够：
- [ ] 理解泛型类、泛型接口的定义方式
- [ ] 知道何时使用一个泛型参数（如 `Box<T>`）
- [ ] 知道何时使用多个泛型参数（如 `BaseRepository<T, ID>`）
- [ ] 理解泛型的"类型参数化"思想
- [ ] 能独立设计一个通用的 DAO 层

---

## 🆘 遇到问题？

### 常见问题

**Q1: 为什么 `MemoryRepository` 要定义为 `abstract`？**
A: 因为它不知道如何获取实体的ID，需要子类实现 `getId()` 和 `setId()` 方法。

**Q2: `Optional<T>` 是什么？**
A: Java 8 引入的容器类，用于优雅地处理可能为 null 的值。它本身也是泛型类！

**Q3: 为什么要用 `ConcurrentHashMap` 而不是 `HashMap`？**
A: 为了线程安全。在实际项目中，Repository 可能被多线程访问。

**Q4: 测试失败怎么办？**
A:
1. 仔细阅读错误信息
2. 检查是否实现了所有 TODO
3. 对照参考答案找差异
4. 在 CLI 中问我具体的问题

---

## 🎯 下一步

完成 Day 1 后，告诉我：
```
"Day 1 完成了！"
```

我会为你开启 Day 2：**通配符实战 (? extends / ? super)**

准备好迎接更大的挑战了吗？ 💪
