# ES 查询模式实战指南

从零开始，一步步调试 7 种在 coupon-search 中最常用的 ES 查询模式。

**前置知识**：会写 Java、会发 HTTP 请求（Postman / curl / IDEA HTTP Client 都行）。
不需要任何 ES 基础。

---

## 0. 环境准备

### 0.1 启动 Elasticsearch

项目里已经有 docker-compose 配置，在项目根目录执行：

```bash
docker compose -f docker/elasticsearch/docker-compose.yml up -d
```

等 30 秒左右，验证 ES 是否启动：

```
GET http://127.0.0.1:9200
```

看到包含 `"tagline" : "You Know, for Search"` 的 JSON 就说明成功了。

### 0.2 启动 Spring Boot 应用

```bash
mvn spring-boot:run
```

应用跑在 `http://localhost:18888`。

### 0.3 一键初始化教学数据

```
POST http://localhost:18888/api/es/pattern/setup
```

这一步做了两件事：
1. 在 ES 中创建一个叫 `pattern_demo_v1` 的索引（类似 MySQL 建表）
2. 往里面插入 6 条优惠券样本数据

返回 `"操作成功"` 就可以开始了。

### 样本数据一览

先心里有数，知道库里有什么：

| couponId | title | 分类 | 优惠金额 | 领取量 | 城市 | 适用门店 | 状态 |
|----------|-------|------|---------|--------|------|---------|------|
| 1001 | 满100减20优惠券 | 满减(1) | 20 | 5280 | 北京 | S001北京朝阳, S002北京海淀 | active |
| 1002 | 新人专享满50减10 | 满减(1) | 10 | 12300 | 上海 | S003上海浦东, S004上海徐汇 | active |
| 1003 | 周末特惠8折券 | 折扣(2) | 0 | 890 | 北京 | S001北京朝阳 | active |
| 1004 | 运费减免券 | 运费(3) | 8 | 45000 | 广州 | S005广州天河, S001北京朝阳 | active |
| 1005 | 满200减50大额优惠券 | 满减(1) | 50 | 3200 | 北京 | S002北京海淀 | expired |
| 1006 | 会员日满减优惠 | 满减(1) | 30 | 7800 | 上海 | S003上海浦东, S005广州天河 | active |

---

## 1. BoolQuery 骨架 —— 所有查询的基础

**要学的概念**：`bool` 查询有两个最重要的槽位：
- `must` — 必须匹配，**参与打分**（搜索关键词放这里）
- `filter` — 必须匹配，**不打分**（精确过滤条件放这里）

为什么区分？因为 ES 搜索不只是"查到/查不到"，还会给每条结果算一个**相关性分数**。
用户搜"满减"，标题是"满100减20"的券和标题是"满减优惠"的券，相关性不一样。
但按"分类=满减券"过滤时，不需要算分数——要么是、要么不是。
filter 不算分数所以更快，而且 ES 会缓存 filter 的结果。

### 请求 1.1：只搜关键词

```
POST http://localhost:18888/api/es/pattern/1-bool?keyword=满减
```

**看返回结果里的这几个地方**：

```
"dsl" 里面：
  "must" 下面有一个 match 查询 → 这就是全文搜索
  "filter" 是空数组 → 没有过滤条件

"total": 3          → 命中 3 条（1001、1005、1006 标题包含"满减"的相关字符）
"hits" 里的 _score   → 每条都有分数，分数越高越相关
```

### 请求 1.2：关键词 + 分类过滤

```
POST http://localhost:18888/api/es/pattern/1-bool?keyword=优惠&couponCategory=1
```

**对比观察**：
- `filter` 里多了 `"term": {"couponCategory": 1}` → 只返回满减券
- 折扣券(2)、运费券(3) 被过滤掉了

### 请求 1.3：只过滤不搜索

```
POST http://localhost:18888/api/es/pattern/1-bool?couponCategory=2
```

**观察**：
- `must` 变成了 `match_all` → 匹配全部文档（因为没有关键词）
- 只靠 filter 筛选出折扣券
- 每条结果的 `_score` 都是 1.0（match_all 给所有文档打 1 分）

### 小结

```
bool 查询 = must（搜索打分） + filter（精确过滤）
↓
coupon-search 里的写法：
  BoolQueryBuilder boolQuery = new BoolQueryBuilder();
  boolQuery.must(matchQuery);     // 关键词
  boolQuery.filter(termQuery);    // 分类过滤
```

---

## 2. Term / Terms 精确匹配

**要学的概念**：
- `term` — 精确匹配单个值（字段值必须 **完全等于** 这个值）
- `terms` — 匹配多个值中的任意一个（等价于 SQL 的 `IN`）
- term 只能用在 `keyword`、`integer`、`long` 等不分词的字段上

### 请求 2.1：按状态过滤

```
POST http://localhost:18888/api/es/pattern/2-term?status=active
```

**观察**：5 条 active 的券被返回，1 条 expired 的 1005 被过滤掉。

### 请求 2.2：按城市过滤

```
POST http://localhost:18888/api/es/pattern/2-term?cityCode=110000
```

**观察**：只返回北京的券（1001、1003、1005）。

### 请求 2.3：组合过滤

```
POST http://localhost:18888/api/es/pattern/2-term?status=active&cityCode=310000
```

**观察**：只返回上海的有效券（1002、1006）。

DSL 里注意看：多个 filter 条件是 **AND** 关系，都在 `filter` 数组里并列放置。

### 请求 2.4：观察 terms（多值 IN）

看 DSL 里的这一段：

```json
"terms": {"couponCategory": [1, 2]}
```

这表示 `couponCategory IN (1, 2)`，满减券和折扣券都会返回，运费券(3)被排除。

### 小结

```
term  → WHERE status = 'active'
terms → WHERE couponCategory IN (1, 2)
↓
coupon-search 里的写法：
  boolQuery.filter(QueryBuilders.termQuery("couponCategory", 1));
```

---

## 3. Range 范围查询

**要学的概念**：
- `range` 查询支持 4 个操作符：`gte`(>=)、`gt`(>)、`lte`(<=)、`lt`(<)
- 可以用在数字、日期等有序类型的字段上
- ES 日期字段支持 `"now"` 关键字，表示当前时间

### 请求 3.1：金额范围

```
POST http://localhost:18888/api/es/pattern/3-range?minDiscount=15
```

**观察**：只返回优惠金额 >= 15 的券（1001=20, 1005=50, 1006=30）。

### 请求 3.2：金额区间

```
POST http://localhost:18888/api/es/pattern/3-range?minDiscount=10&maxDiscount=30
```

**观察**：10 <= discountAmount <= 30，返回 1001(20), 1002(10), 1006(30)。

### 请求 3.3：日期范围（只查有效券）

```
POST http://localhost:18888/api/es/pattern/3-range?validNow=true
```

**观察 DSL**：

```json
"range": {"startTime": {"lte": "now"}}   → 已经开始
"range": {"endTime": {"gte": "now"}}     → 还没结束
```

1005 是 expired 的（endTime 已过），被 range 过滤掉了。

### 请求 3.4：组合

```
POST http://localhost:18888/api/es/pattern/3-range?minDiscount=20&validNow=true
```

**观察**：同时满足"优惠>=20"和"当前有效"的只有 1001(20) 和 1006(30)。
1005 虽然优惠金额是 50，但已过期。

### 小结

```
range → WHERE discountAmount >= 10 AND discountAmount <= 30
        WHERE startTime <= NOW() AND endTime >= NOW()
↓
coupon-search 里的写法：
  boolQuery.filter(QueryBuilders.rangeQuery("useStartTime").gte(startTime));
  boolQuery.filter(QueryBuilders.rangeQuery("useEndTime").lte(endTime));
```

---

## 4. Nested 嵌套查询

**要学的概念**：

这是最不好理解的一个，先看问题再看方案。

**问题**：一张券可以在多个门店使用。1001 能在 S001(北京朝阳) 和 S002(北京海淀) 两家店用。

如果用普通数组存门店，ES 会把数据"扁平化"：
```
code:  ["S001", "S002"]
name:  ["北京朝阳店", "北京海淀店"]
```

这时候搜索 `code=S001 AND name=北京海淀店` 会错误命中！
因为 S001 和 北京海淀店 都存在于数组中，ES 不知道它们不是一对。

**方案**：把 stores 声明为 `nested` 类型。
nested 会把每个门店对象存为独立的内部文档，字段之间的绑定关系不会丢。
但查询时必须用 `nested query`，告诉 ES "我要查的是嵌套对象内部的字段"。

### 请求 4.1：按门店编码过滤

```
POST http://localhost:18888/api/es/pattern/4-nested?storeCode=S001
```

**观察**：返回能在 S001(北京朝阳店) 使用的券：1001, 1003, 1004（看样本数据表验证）。

**看 DSL**：
```json
{
  "nested": {
    "path": "stores",                         ← 指定嵌套字段的路径
    "score_mode": "none",                     ← 不参与评分（纯过滤）
    "query": {
      "term": {"stores.code": "S001"}         ← 字段名要带路径前缀
    }
  }
}
```

三个关键点：
1. `path` 必须填
2. 嵌套内部的字段名要写全路径：`stores.code` 而不是 `code`
3. `score_mode: none` 表示这个嵌套查询只做过滤，不影响文档的相关性分数

### 请求 4.2：按法人编码过滤

```
POST http://localhost:18888/api/es/pattern/4-nested?corporationCode=CORP_BJ
```

**观察**：返回北京法人(CORP_BJ)下所有门店可用的券：1001, 1003, 1004, 1005。

### 请求 4.3：门店 + 法人 组合

```
POST http://localhost:18888/api/es/pattern/4-nested?storeCode=S005&corporationCode=CORP_GZ
```

**观察**：两个独立的 nested query 在 filter 中并列，是 AND 关系。

### 小结

```
nested query 三要素：path + query + score_mode
用在一对多的嵌套对象上，防止字段"串联"误匹配
↓
coupon-search 里的写法：
  boolQuery.filter(
      QueryBuilders.nestedQuery("stores",
          QueryBuilders.termQuery("stores.code", storeCode),
          ScoreMode.None));
```

---

## 5. DisMax 最佳匹配

**要学的概念**：

用户搜 "满减"，用哪种查询方式最好？
- `prefix`（前缀匹配）→ 能匹配 "满减优惠券"（以"满减"开头）
- `match`（分词匹配）→ 能匹配 "满100减20"（分词后包含相关词）
- `match_phrase`（短语匹配）→ 能匹配标题中紧挨着出现"满减"的

答案是：**都用，取分数最高的那个**。这就是 `dis_max`。

`dis_max` 会执行所有子查询，最终得分 = 得分最高的子查询的分数。
通过 `boost` 参数给不同查询设置权重：
- 前缀精确匹配命中 → 给最高分（最相关）
- 短语匹配命中 → 第二高
- 普通分词匹配 → 基础分

### 请求 5.1：短关键词

```
POST http://localhost:18888/api/es/pattern/5-dismax?keyword=满减
```

**看 DSL 里的 queries 数组**：只有 2 个子查询（prefix + match）。
短关键词不需要 phrase 和 fuzzy，策略保持简单。

### 请求 5.2：长关键词

```
POST http://localhost:18888/api/es/pattern/5-dismax?keyword=满减优惠券
```

**看 DSL 里的 queries 数组**：变成了 4 个子查询。

```json
queries: [
  prefix       → boost=5.0  （前缀完全匹配，权重最高）
  match_phrase → boost=4.0  （短语顺序匹配，次高）
  match        → boost=2.0  （分词匹配，普通）
  match(fuzzy) → boost=0.5  （模糊容错，权重最低）
]
```

**对比两条结果的 _score**，想想为什么某条的分更高。

### 请求 5.3：观察模糊匹配的作用

```
POST http://localhost:18888/api/es/pattern/5-dismax?keyword=运费减兔券
```

"兔"是"免"的错别字。看看 fuzzy 子查询能否容错匹配到"运费减免券"。
（fuzziness=1 表示允许 1 个字符的编辑距离）

### 请求 5.4：对比不用 DisMax 的效果

回到模式 1 试一下同样的关键词：

```
POST http://localhost:18888/api/es/pattern/1-bool?keyword=满减优惠券
```

对比模式 5 的结果，注意排序差异。
模式 1 只用了一个 match，模式 5 用了多种策略择优。

### 小结

```
dis_max = 多种搜索策略同时执行，取最高分
通过 boost 控制各策略的权重优先级
通过关键词长度动态选择策略组合
↓
coupon-search 里的写法（BaseChineseQuery 接口）：
  DisMaxQueryBuilder disMax = new DisMaxQueryBuilder();
  disMax.add(QueryBuilders.prefixQuery(field + ".lowercase", keyword).boost(10F));
  disMax.add(QueryBuilders.matchPhraseQuery(field + ".smart_word", keyword).boost(6F));
  disMax.add(QueryBuilders.matchQuery(field + ".max_word", keyword).boost(1F));
```

---

## 6. 排序 + 分页

**要学的概念**：
- `sort` 数组指定排序规则，按数组顺序优先级递减
- `from` + `size` 实现分页：from = (页码-1) * 每页大小
- `_score` 是 ES 计算的相关性分数，可以作为排序字段

### 请求 6.1：按领取量降序

```
POST http://localhost:18888/api/es/pattern/6-sort?sort=receive_desc
```

**观察结果顺序**：1004(45000) > 1002(12300) > 1006(7800) > 1001(5280) > ...

### 请求 6.2：按优惠金额降序

```
POST http://localhost:18888/api/es/pattern/6-sort?sort=discount_desc
```

**观察结果顺序**：1005(50) > 1006(30) > 1001(20) > ...

### 请求 6.3：分页

```
POST http://localhost:18888/api/es/pattern/6-sort?sort=receive_desc&pageNo=1&pageSize=2
```

**观察**：`from=0, size=2`，只返回前 2 条。

```
POST http://localhost:18888/api/es/pattern/6-sort?sort=receive_desc&pageNo=2&pageSize=2
```

**观察**：`from=2, size=2`，跳过前 2 条，返回第 3~4 条。

### 请求 6.4：二级排序

看 DSL 里的 sort 数组，注意有两个排序条件：

```json
"sort": [
  {"discountAmount": {"order": "desc"}},    ← 主排序
  {"_score": {"order": "desc"}}             ← 二级排序（金额相同时按相关性）
]
```

### 小结

```
sort 数组按顺序依次排序，from+size 实现分页
↓
coupon-search 里的写法：
  searchSourceBuilder.sort("parValue", SortOrder.DESC);
  searchSourceBuilder.from(pageSize * (pageIndex - 1));
  searchSourceBuilder.size(pageSize);
```

---

## 7. 综合查询 —— 真实业务场景

**把模式 1~6 全部组合在一起**，这就是 coupon-search 中 `SearchCouponEsServiceImpl.search()` 的完整逻辑。

### 请求 7.1：搜索北京朝阳店里当前有效的满减券

```
POST http://localhost:18888/api/es/pattern/7-full?keyword=满减&storeCode=S001&validNow=true
```

**看 DSL 结构**：

```
query.bool
  ├── must       → dis_max（关键词搜索）    ← 模式 5
  └── filter
        ├── nested（门店过滤 S001）          ← 模式 4
        ├── range（startTime <= now）        ← 模式 3
        └── range（endTime >= now）          ← 模式 3
sort             → 按 _score 降序           ← 模式 6
from + size      → 分页                     ← 模式 6
```

逐一验证：搜"满减"、只要 S001 能用的、当前有效的。

### 请求 7.2：上海法人下的有效券，按领取量排序

```
POST http://localhost:18888/api/es/pattern/7-full?corporationCode=CORP_SH&validNow=true&sort=receive_desc
```

### 请求 7.3：满减类别 + 关键词 + 分页

```
POST http://localhost:18888/api/es/pattern/7-full?keyword=优惠&couponCategory=1&sort=discount_desc&pageSize=2
```

### 请求 7.4：无关键词，纯过滤浏览

```
POST http://localhost:18888/api/es/pattern/7-full?couponCategory=1&validNow=true&sort=receive_desc
```

**观察**：没有 keyword 时 must 变成 match_all，_score 全是 1.0，排序完全由 receiveCount 决定。

---

## 8. 文档 CRUD 操作

这部分对应 coupon-search 里通过 RocketMQ 消费消息后写入 ES 的操作。

### 8.1 新增一条文档

```
POST http://localhost:18888/api/es/pattern/doc
Content-Type: application/json

{
  "couponId": 2001,
  "title": "测试新增的优惠券",
  "couponCategory": 1,
  "discountAmount": 99.0,
  "receiveCount": 0,
  "startTime": 1706659200000,
  "endTime": 1738281600000,
  "status": "active",
  "cityCode": "110000",
  "stores": [
    {"code": "S001", "name": "北京朝阳店", "corporationCode": "CORP_BJ"}
  ]
}
```

然后搜索验证它已经被索引：

```
POST http://localhost:18888/api/es/pattern/1-bool?keyword=测试新增
```

### 8.2 部分更新（只改某些字段）

只改领取量，其他字段不动：

```
PATCH http://localhost:18888/api/es/pattern/doc/2001
Content-Type: application/json

{"receiveCount": 888}
```

### 8.3 删除文档

```
DELETE http://localhost:18888/api/es/pattern/doc/2001
```

再搜 "测试新增" 就找不到了。

---

## 9. 清理

测试完后删掉教学索引：

```
DELETE http://localhost:18888/api/es/pattern/cleanup
```

想重新来一遍就再执行 `POST /api/es/pattern/setup`。

---

## 概念速查表

| ES 概念 | 类比 MySQL | coupon-search 中的用法 |
|---------|-----------|----------------------|
| Index | 表 | `delivery_store_coupon` |
| Document | 一行数据 | `IndexDeliveryStoreCouponData` 对象 |
| Mapping | 表结构（CREATE TABLE） | `delivery_coupon_mapping.json` |
| keyword 类型 | VARCHAR + 索引，精确匹配 | 门店编码、城市编码 |
| text 类型 | 全文检索字段，会被分词 | 券标题 |
| nested 类型 | 子表（一对多） | 门店列表 |
| bool 查询 | WHERE 子句 | `BoolQueryBuilder` |
| must | WHERE ... AND ...（影响排序） | 关键词搜索 |
| filter | WHERE ... AND ...（不影响排序，更快） | 分类、门店、日期过滤 |
| term | WHERE col = 'val' | `QueryBuilders.termQuery()` |
| terms | WHERE col IN ('a','b') | `QueryBuilders.termsQuery()` |
| match | 全文搜索（分词后匹配） | `QueryBuilders.matchQuery()` |
| match_phrase | 短语搜索（保持词序） | `QueryBuilders.matchPhraseQuery()` |
| prefix | WHERE col LIKE 'val%' | `QueryBuilders.prefixQuery()` |
| range | WHERE col BETWEEN a AND b | `QueryBuilders.rangeQuery()` |
| nested query | JOIN 子表查询 | `QueryBuilders.nestedQuery()` |
| dis_max | 多种搜索方式取最优 | `DisMaxQueryBuilder` |
| _score | 相关性分数（越高越相关） | 默认按此排序 |
| boost | 给某个条件加权 | prefix boost=10, phrase boost=6 |
| from + size | LIMIT offset, count | `searchSourceBuilder.from().size()` |
| Bulk API | 批量 INSERT | `EsBaseOperate.addToEs()` |
| _update API | UPDATE SET col=val | `baseElasticSearchService.updateByDoc()` |
