# 🔥 Day 2: 通配符实战 (? extends / ? super)

## Day 1 回顾
昨天你学会了：
- 用泛型类 `GenericBox<T>` 消除重复代码
- `GenericBox<User>` 和 `GenericBox<Product>` 是**不同的类型**

## Day 2 要解决的问题

### 问题1：能把 `List<Dog>` 赋值给 `List<Animal>` 吗？

```java
class Animal {}
class Dog extends Animal {}

List<Dog> dogs = new ArrayList<>();
List<Animal> animals = dogs;  // ❌ 编译错误！

// 为什么？因为 List<Dog> 和 List<Animal> 是两个完全独立的类型！
```

**思考**：Dog是Animal的子类，但 `List<Dog>` **不是** `List<Animal>` 的子类！

### 问题2：如何写一个方法，既能处理 `List<Dog>`，又能处理 `List<Cat>`？

```java
// ❌ 错误的尝试
public void printAnimals(List<Animal> animals) {
    // ...
}

List<Dog> dogs = ...;
printAnimals(dogs);  // ❌ 编译错误！List<Dog> 不能传给 List<Animal>
```

**解决方案**：通配符 `?`

```java
// ✅ 正确的方式
public void printAnimals(List<? extends Animal> animals) {
    // 可以传入 List<Dog>、List<Cat>、List<Animal>
}
```

---

## 核心知识点

### 1. 上界通配符 `? extends T` (只读，生产者)

**含义**：某个类型，它是 T 或 T 的子类

```java
List<? extends Animal> animals;

// ✅ 可以赋值
animals = new ArrayList<Animal>();
animals = new ArrayList<Dog>();
animals = new ArrayList<Cat>();

// ✅ 可以读取（保证是Animal或其子类）
Animal animal = animals.get(0);  // OK

// ❌ 不能写入（编译器不知道具体是哪个子类）
animals.add(new Dog());    // ❌ 编译错误！
animals.add(new Animal()); // ❌ 编译错误！
```

**记忆口诀**：`extends` = 只读 = 生产者(Producer)

### 2. 下界通配符 `? super T` (只写，消费者)

**含义**：某个类型，它是 T 或 T 的父类

```java
List<? super Dog> list;

// ✅ 可以赋值
list = new ArrayList<Dog>();
list = new ArrayList<Animal>();
list = new ArrayList<Object>();

// ✅ 可以写入（保证能存Dog及其子类）
list.add(new Dog());      // OK
list.add(new Puppy());    // OK（Puppy extends Dog）

// ❌ 不能读取为具体类型（不知道具体是哪个父类）
Dog dog = list.get(0);    // ❌ 编译错误！
Animal a = list.get(0);   // ❌ 编译错误！
Object obj = list.get(0); // ✅ 只能读取为Object
```

**记忆口诀**：`super` = 只写 = 消费者(Consumer)

### 3. PECS原则

**Producer Extends, Consumer Super**

- 如果你需要**从集合读取**数据 → 用 `? extends T`
- 如果你需要**往集合写入**数据 → 用 `? super T`
- 如果既要读又要写 → 不用通配符，用 `T`

---

## 实战练习

### 场景1：数据复制问题
你要实现一个方法，把一个列表的数据复制到另一个列表。

```java
// 需求：把 List<Dog> 复制到 List<Animal>
// 需求：把 List<Integer> 复制到 List<Number>
```

### 场景2：数据收集问题
你要实现一个方法，把数据收集到一个列表中。

```java
// 需求：把 Dog 添加到 List<Animal>
// 需求：把 Integer 添加到 List<Number>
```

### 场景3：类型转换器
实现一个通用的数据转换工具。

---

## 学习路径

1. **先看问题**：`before/` 目录展示没有通配符时的困境
2. **理解通配符**：通过例子理解 `extends` 和 `super`
3. **动手实践**：`after/` 目录用通配符重构
4. **验证理解**：运行测试，回答思考题

---

## 开始学习

进入 `before/` 目录，运行 `WildcardProblemDemo.java`，感受问题！

**准备好迎接挑战了吗？** 🚀
