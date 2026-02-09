# 分库分表核心知识点 — 面试复习指南

> 基于永旺（AEON）项目真实分库分表实现，简化为 bruceDemo 最小化示例。
> Author: claude code

---

## 目录

1. [什么时候该分库分表？](#1-什么时候该分库分表)
2. [分片键怎么选？](#2-分片键怎么选)
3. [三种分片算法详解](#3-三种分片算法详解)
4. [ShardingSphere 配置方式](#4-shardingsphere-配置方式)
5. [分库分表常见坑](#5-分库分表常见坑)
6. [面试高频问答](#6-面试高频问答)
7. [代码对照表](#7-代码对照表永旺-vs-brucedemo)

---

## 1. 什么时候该分库分表？

### 分表指标

| 指标 | 阈值 | 说明 |
|------|------|------|
| 单表行数 | > 2000万 | B+Tree 层级增加，索引效率下降 |
| 单表大小 | > 10GB | 影响 DDL 操作（加字段、加索引） |
| 查询 RT | > 100ms | 优化索引后仍然慢 |
| TPS | > 5000/s | 单库连接池瓶颈 |

### 分库指标

| 指标 | 阈值 | 说明 |
|------|------|------|
| 连接数 | > 3000 | 单实例连接数上限 |
| QPS | > 20000 | 单 MySQL 实例瓶颈 |
| 磁盘 IOPS | 打满 | 需要横向扩展存储 |

### 先尝试的优化手段（避免过早分表）

1. 优化 SQL + 索引
2. 读写分离
3. 冷热数据归档
4. 缓存层（Redis）
5. **以上都试了还不行 → 分库分表**

---

## 2. 分片键怎么选？

### 选择原则

```
核心原则：让 90% 的查询都能精确路由到单表，避免全表扫描。
```

| 原则 | 说明 | 示例 |
|------|------|------|
| **高频查询字段** | 最常出现在 WHERE 条件中的字段 | 券表用 member_id |
| **数据分布均匀** | 取模后各表数据量差不多 | 自增 ID、会员 ID |
| **避免跨表查询** | 同一业务的数据尽量在同一个分片 | 同一会员的券在同一张表 |
| **避免热点** | 不要用时间戳（最近的表扛所有写入） | 订单表用 user_id 而非 order_time |

### 永旺项目分片键选择

| 业务表 | 分片键 | 选择理由 |
|--------|--------|----------|
| 券发放记录 | `member_id` | 90% 查询是"查某个会员的券"，member_id 分布均匀 |
| 券聚合表 | `coupon_template_id` | 核心查询是"某券模板下的发放汇总" |
| 活动记录 | `created_at` | 按月归档，历史数据可直接 DROP TABLE |
| 订单表 | `store_code` | 按门店维度查询最频繁 |

### 分片键设计技巧：券编号嵌入会员信息

```
券编号生成规则：前缀 + 时间戳 + member_id后8位
例如：CP20250315100000 12345678
                                ^^^^^^^^
                               member_id后8位

好处：coupon_no 末8位 % tableNum == member_id % tableNum
     → 按 coupon_no 查也能精确路由，不用全表扫描
```

---

## 3. 三种分片算法详解

### 3.1 ComplexKeysShardingAlgorithm — 多分片键取模

**适用场景**：一张表需要支持多个查询入口（如按 member_id 或按 coupon_no 查）

**核心公式（单库多表版）**：
```
tableIndex = value % tableNum

示例（4张表）：
member_id=100 → 100 % 4 = 0 → coupon_give_record_00
member_id=101 → 101 % 4 = 1 → coupon_give_record_01
member_id=102 → 102 % 4 = 2 → coupon_give_record_02
member_id=103 → 103 % 4 = 3 → coupon_give_record_03
```

**核心公式（多库多表版，永旺真实配置 16库×32表）**：
```
totalShards = dbNum × tableNum = 16 × 32 = 512

dbIndex    = value % 512 % 16        → 得到库编号 (0~15)
tableIndex = (value % 512) / 16 % 32 → 得到表编号 (0~31)

示例：
member_id后8位 = 12345678
12345678 % 512 = 334
dbIndex    = 334 % 16 = 14  → coupon-member-db-14
tableIndex = 334 / 16 % 32 = 20  → coupon_give_record_20
```

**代码路径**：
- bruceDemo: `com.bruce.shardingdemo.algorithm.MemberModuloAlgorithm`
- 永旺: `CouponMemberDbAlgorithm` + `CouponMemberDbTableAlgorithm`

```
┌─────────────┐     member_id=100
│ 逻辑表       │     ──────────────►  100 % 4 = 0
│ coupon_give  │
│ _record      │     ┌──────────────────────────┐
└─────────────┘     │  coupon_give_record_00  ← │
                     │  coupon_give_record_01    │
                     │  coupon_give_record_02    │
                     │  coupon_give_record_03    │
                     └──────────────────────────┘
```

### 3.2 PreciseShardingAlgorithm — 单分片键精确匹配

**适用场景**：只需要一个分片键，且查询条件是精确匹配（=、IN）

**核心公式**：
```
tableIndex = templateId % tableNum

示例（4张表）：
templateId=1000 → 1000 % 4 = 0 → gather_00
templateId=1001 → 1001 % 4 = 1 → gather_01
templateId=1002 → 1002 % 4 = 2 → gather_02
templateId=1003 → 1003 % 4 = 3 → gather_03
```

**代码路径**：
- bruceDemo: `com.bruce.shardingdemo.algorithm.TemplateIdPreciseAlgorithm`
- 永旺: `CouponGatherDbHitAlgorithm` + `CouponGatherDbTableHitAlgorithm`

### 3.3 Precise + Range ShardingAlgorithm — 按时间按月归档

**适用场景**：按时间维度分表，需要支持精确查询和范围查询

**核心公式**：
```
tableSuffix = SimpleDateFormat("yyyyMM").format(date)

精确路由（INSERT / WHERE date = ?）：
  2025-03-15 → order_record_202503

范围路由（WHERE date BETWEEN ? AND ?）：
  2025-03-01 ~ 2025-05-31 → [order_record_202503, order_record_202504, order_record_202505]
```

**代码路径**：
- bruceDemo: `com.bruce.shardingdemo.algorithm.ArchiveMonthlyAlgorithm`
- 永旺: `ArchiveMonthlyAlgorithm`（营销中心）

```
              ┌──────── 精确路由 ────────┐
              │                          │
   date=2025-03-15  →  order_record_202503 (单表)
              │                          │
              └──────────────────────────┘

              ┌──────── 范围路由 ────────┐
              │                          │
   BETWEEN    │  order_record_202503     │
   03-01      │  order_record_202504     │  → 3张表
   AND 05-31  │  order_record_202505     │
              │                          │
              └──────────────────────────┘
```

### 三种算法对比

| 特性 | Complex | Precise | Precise + Range |
|------|---------|---------|-----------------|
| 接口 | `ComplexKeysShardingAlgorithm` | `PreciseShardingAlgorithm` | 两者都实现 |
| 分片键数量 | 多个 | 1个 | 1个 |
| 支持的 SQL | =, IN | =, IN | =, IN, BETWEEN, >, < |
| 返回结果 | `Collection<String>` | `String` | Precise→String, Range→Collection |
| 配置类 | `ComplexShardingStrategyConfiguration` | `StandardShardingStrategyConfiguration` | `StandardShardingStrategyConfiguration` |
| 使用场景 | 会员券(member_id + coupon_no) | 聚合表(template_id) | 按月归档(created_at) |

---

## 4. ShardingSphere 配置方式

### 永旺项目使用 Java API 配置（非 YAML）

**原因**：
1. 分片参数从 Apollo 配置中心动态读取（dbNum、tableNum）
2. 多个业务模块有不同的分片策略，Java API 更灵活
3. 运行时可以动态调整，不用重启

### 配置核心流程

```java
// 第1步：创建数据源 Map
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("ds0", realDataSource);
// 多库场景：dataSourceMap.put("ds1", anotherDataSource);

// 第2步：创建分片规则
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

// 第3步：配置逻辑表 → 物理表映射 + 分片算法
TableRuleConfiguration tableRule = new TableRuleConfiguration(
    "coupon_give_record",                    // 逻辑表名
    "ds0.coupon_give_record_00,...,ds0.coupon_give_record_03");  // 物理表

tableRule.setTableShardingStrategyConfig(
    new ComplexShardingStrategyConfiguration(
        "member_id,coupon_no",               // 分片键
        new MemberModuloAlgorithm(4, 8)));   // 分片算法

shardingRuleConfig.getTableRuleConfigs().add(tableRule);

// 第4步：创建分片数据源
Properties props = new Properties();
props.put("sql.show", true);  // 调试用
DataSource ds = ShardingDataSourceFactory.createDataSource(
    dataSourceMap, shardingRuleConfig, props);
```

### 多数据源隔离

```
┌─────────────────────────────┐
│       RyanDemoApplication    │
│  @MapperScan("com.ryan.**") │  → 默认 SqlSessionFactory (dynamic-datasource)
└─────────────────────────────┘

┌─────────────────────────────┐
│  ShardingDataSourceConfig    │
│  @MapperScan(               │
│    "com.bruce.shardingdemo"  │  → shardingSqlSessionFactory (ShardingSphere)
│    sqlSessionFactoryRef=     │
│    "shardingSqlSessionFactory")│
└─────────────────────────────┘
```

**关键点**：必须用 `MybatisSqlSessionFactoryBean`（MyBatis-Plus 提供），不能用原生 `SqlSessionFactoryBean`，否则 `BaseMapper` 的通用方法不会被注入。

---

## 5. 分库分表常见坑

### 5.1 跨表 JOIN

**问题**：分片后，原来的 JOIN 可能涉及不同物理表，无法直接执行。

**解决方案**：
1. **绑定表**：配置 BindingTableRule，让关联表使用相同的分片键和算法
2. **冗余字段**：把需要 JOIN 的字段冗余到分片表中
3. **应用层 JOIN**：在代码中分别查询再合并
4. **全局表**：小表（如配置表）配置为广播表，每个库都有完整副本

### 5.2 分布式事务

**问题**：跨库操作无法用本地事务保证一致性。

**解决方案**：
1. **柔性事务**：最终一致性（推荐），通过消息队列 + 重试机制
2. **XA 事务**：ShardingSphere 支持，但性能差
3. **设计规避**：让同一业务操作的数据落在同一个分片（如同一会员的券操作）

### 5.3 分页查询

**问题**：`ORDER BY + LIMIT` 在多表时需要每张表都查出数据再合并排序。

```sql
-- 逻辑 SQL
SELECT * FROM order_record ORDER BY id LIMIT 100, 10;

-- 实际执行（12张表每张都要查 110 条，然后合并排序取10条）
-- 性能灾难！
```

**解决方案**：
1. **禁止深度分页**：限制 offset 最大值（如 10000）
2. **游标分页**：`WHERE id > lastId LIMIT 10`（推荐）
3. **二次查询**：先查各表的边界值，再精确查询
4. **ES 辅助**：用 Elasticsearch 做分页查询，MySQL 只做主键回查

### 5.4 全局 ID 生成

**问题**：自增 ID 在多表场景会冲突。

**解决方案**：
1. **雪花算法**：ShardingSphere 内置支持
2. **号段模式**：从数据库批量获取 ID 段（如美团 Leaf）
3. **UUID**：简单但无序，影响索引性能
4. **Redis INCR**：简单高效，但依赖 Redis

### 5.5 扩容（最头疼的问题）

**问题**：从 4 表扩到 8 表，原来 `id % 4 = 0` 的数据需要重新路由。

**解决方案**：
1. **翻倍扩容**：4→8→16，只需迁移一半数据
2. **一致性哈希**：减少扩容时的数据迁移量
3. **预估容量**：一开始就分够（如永旺分 512 个分片，几年内不用扩容）
4. **双写迁移**：旧表和新表双写，验证一致后切换

---

## 6. 面试高频问答

### Q1: 你们项目分库分表的背景是什么？

> 永旺券中心，会员量 2000万+，每天券发放流水百万级。单表超过 5000万行后，
> 查询 RT 从 20ms 劣化到 200ms+，索引优化和读写分离都无法解决。
> 于是引入 ShardingSphere-JDBC 做分库分表。

### Q2: 分了多少库多少表？怎么决定的？

> 会员库：16库 × 32表 = 512个分片。
> 按未来 3 年数据量预估：日均 100万条 × 365天 × 3年 ≈ 10亿条。
> 10亿 / 512 ≈ 200万/分片，远低于 2000万的单表阈值。
> 取 2 的幂次方是为了将来翻倍扩容方便。

### Q3: 分片键为什么选 member_id？

> 90% 的查询是"查某个会员的所有券"，member_id 出现在几乎每个 WHERE 条件中。
> member_id 是自增的数字型，取模后数据分布非常均匀。
> 同一个会员的所有券在同一张表里，避免了跨表查询。

### Q4: 如果查询条件没有分片键怎么办？

> 1. 设计上尽量保证核心查询带分片键
> 2. 对于管理后台的模糊查询，走 Elasticsearch，不走分片表
> 3. 实在需要全表扫描的场景，ShardingSphere 会并行查所有分片，但要限制频率

### Q5: 分布式 ID 怎么生成的？

> 用雪花算法（Snowflake），ShardingSphere 内置支持。
> 配置 KeyGeneratorConfiguration 指定 SNOWFLAKE 策略，
> worker-id 通过 IP 后两段计算，避免多节点冲突。

### Q6: 遇到过什么坑？

> 1. **深度分页性能差**：LIMIT 100000,10 会导致每张表都查 100010 条。
>    解决：改为游标分页 `WHERE id > lastId LIMIT 10`。
> 2. **跨库事务**：券发放涉及扣减库存（聚合库）和写入发放记录（会员库），
>    两个不同的分片数据源。解决：用消息队列做最终一致性。
> 3. **物理表预建**：按月分表的场景，忘了提前建下一年的表导致写入报错。
>    解决：定时任务提前创建未来 3 个月的表。

### Q7: ShardingSphere 的路由流程是怎样的？

```
应用代码
  ↓ SQL
ShardingSphere SQL Parser（解析 SQL，提取表名 + 分片键值）
  ↓
Sharding Router（根据分片算法计算目标分片）
  ↓
SQL Rewriter（将逻辑表名替换为物理表名）
  ↓
SQL Executor（并行发送到各个物理数据源）
  ↓
Result Merger（合并多个分片的结果集）
  ↓
返回给应用
```

### Q8: 如果让你重新设计，会有什么改进？

> 1. 考虑用 ShardingSphere-Proxy 替代 JDBC 模式，
>    对应用完全透明，运维更方便
> 2. 分片键嵌入更多信息（如业务类型前缀），支持更灵活的路由
> 3. 引入 ShardingSphere 的数据迁移工具 ShardingSphere-Scaling，
>    自动化扩容流程

---

## 7. 代码对照表（永旺 vs bruceDemo）

### 分片算法

| 永旺项目 | bruceDemo | 说明 |
|----------|-----------|------|
| `CouponMemberDbAlgorithm` | `MemberModuloAlgorithm` | 会员券分表（16库×32表 → 单库4表） |
| `CouponMemberDbTableAlgorithm` | 同上（合并） | 永旺分库+分表两个类，bruceDemo 合一 |
| `CouponGatherDbHitAlgorithm` | `TemplateIdPreciseAlgorithm` | 聚合表分表（8库×8表 → 单库4表） |
| `ArchiveMonthlyAlgorithm` | `ArchiveMonthlyAlgorithm` | 按月归档（同名） |

### 配置类

| 永旺项目 | bruceDemo | 说明 |
|----------|-----------|------|
| `CouponMemberShardingJdbcConfig` | `ShardingDataSourceConfig` | 会员库分片配置 |
| `CouponGatherShardingJdbcConfig` | 同上（合并） | 聚合库分片配置 |
| 营销中心配置 | 同上（合并） | 按月归档配置 |

### 文件清单

```
src/main/java/com/bruce/shardingdemo/
├── algorithm/
│   ├── MemberModuloAlgorithm.java          # ComplexKeysShardingAlgorithm
│   ├── TemplateIdPreciseAlgorithm.java     # PreciseShardingAlgorithm
│   └── ArchiveMonthlyAlgorithm.java        # Precise + Range
├── config/
│   └── ShardingDataSourceConfig.java       # ShardingSphere Java API 配置
├── entity/
│   ├── CouponGiveRecord.java               # 会员券实体
│   ├── CouponGiveRecordGather.java         # 聚合表实体
│   └── OrderRecord.java                    # 订单实体
└── mapper/
    ├── CouponGiveRecordMapper.java
    ├── CouponGiveRecordGatherMapper.java
    └── OrderRecordMapper.java

src/test/java/com/bruce/shardingdemo/
├── ShardingAlgorithmTest.java              # 纯单元测试（不依赖 Spring）
└── ShardingIntegrationTest.java            # 集成测试（依赖 MySQL）

src/main/resources/sql/
└── sharding_tables.sql                     # 建表脚本（20张物理表）
```

### 运行方式

```bash
# 1. 建表
mysql -u root -p study < src/main/resources/sql/sharding_tables.sql

# 2. 纯算法测试（不需要数据库）
mvn test -Dtest=com.bruce.shardingdemo.ShardingAlgorithmTest

# 3. 集成测试（需要 MySQL）
mvn test -Pdev -Dtest=com.bruce.shardingdemo.ShardingIntegrationTest

# 4. 查看路由日志（sql.show=true）
# 控制台输出示例：
# Logic SQL: INSERT INTO coupon_give_record ...
# Actual SQL: ds0 ::: INSERT INTO coupon_give_record_01 ...
```

---

## 附录：永旺项目真实配置参数

| 业务 | 库数 | 表数/库 | 总分片数 | 分片键 |
|------|------|---------|----------|--------|
| 券会员库 | 16 | 32 | 512 | member_id (后8位) |
| 券聚合库 | 8 | 8 | 64 | coupon_template_id |
| 活动记录 | 1 | 按月 | 12+/年 | created_at |
| 印花会员库 | 16 | 32 | 512 | member_id |
| 订单库 | 16 | 32 | 512 | store_code (hashCode) |
