这个问题问到点上了，得从 ES 的底层存储结构说起。

**ES 底层是 Lucene，Lucene 本质上是"扁平的"**

Lucene 不认识 JSON 的嵌套结构，它只认识**字段名 → 值**这种扁平的键值对。所以 ES 拿到你的 JSON 文档后，必须把它"压扁"才能存进 Lucene。

**普通 object 的处理方式**

你写入的 JSON：
```json
{
  "product": "iPhone",
  "coupons": [
    { "type": "满减", "threshold": 5000 },
    { "type": "折扣", "threshold": 2000 }
  ]
}
```

ES 默认把 coupons 当作 object 类型，存到 Lucene 时变成：
```
product        → ["iPhone"]
coupons.type   → ["满减", "折扣"]
coupons.threshold → [5000, 2000]
```

就是简单粗暴地用**点号拼接路径**作为字段名，然后所有值塞进一个数组。第一个 type 和第一个 threshold "属于同一张券"这个信息，**在存储层面根本不存在**。

**nested 的处理方式**

如果你把 coupons 字段映射为 nested 类型，ES 会把每个数组元素存成**独立的隐藏文档**：

```
// 主文档
文档0: { product → "iPhone" }

// 隐藏的嵌套文档，带有指向父文档的关联
文档1: { coupons.type → "满减", coupons.threshold → 5000, _parent → 文档0 }
文档2: { coupons.type → "折扣", coupons.threshold → 2000, _parent → 文档0 }
```

每张券是独立文档，内部字段的关联关系自然就保留了。

**所以区别的本质是**

| | object | nested |
|---|--------|--------|
| 存储方式 | 扁平化成数组，丢失元素边界 | 每个元素存成独立文档 |
| 文档数量 | 1 个 | 1 + N 个（N 是数组长度）|
| 查询能力 | 无法查"同一个对象内的多条件" | 可以 |
| 性能 | 更快更省空间 | 更慢更占空间 |

不是 ES 故意要扁平化，而是**底层 Lucene 的限制**决定了必须这样。nested 是 ES 在 Lucene 之上做的一层抽象，用多文档+关联关系来模拟嵌套结构。