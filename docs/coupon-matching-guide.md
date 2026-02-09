# 券匹配条件 学习指南

## 这个文档解决什么问题

你知道"券能不能用"，但不清楚系统是怎么一步步判断出"能用/不能用"的。

本文档拆解永旺真实的券匹配逻辑，从最简单的版本（你已有的 `CouponEngine#isAvailable`）到完整版本（永旺 `StoreCouponHelper`），逐层讲解每个匹配条件的作用和实现。

## 先跑测试

```bash
mvn test "-Dtest=com.bruce.coupondemo.CouponMatcherTest"
```

25 个测试，覆盖 5 层匹配条件。每个测试的 `@DisplayName` 就是它在验证的场景。

---

## 匹配的本质：一条校验链

券匹配就是一个 **if-else 链**，从上到下逐个检查，任何一个条件不满足就返回不可用原因：

```java
// CouponMatcher#check() 的核心结构
public String check(CouponTemplate tpl, MatchContext ctx) {
    String reason;

    reason = checkTime(tpl, ctx.getNow());       // 第1层
    if (reason != null) return reason;

    reason = checkStore(tpl, ctx.getStoreCode()); // 第2层
    if (reason != null) return reason;

    reason = checkChannel(tpl, ctx.getChannel()); // 第3层
    if (reason != null) return reason;

    List<MatchCartItem> matched = filterMatchedItems(tpl, ctx.getItems());

    reason = checkProductCondition(tpl, matched); // 第4层
    if (reason != null) return reason;

    reason = checkThreshold(tpl, matched);        // 第5层
    if (reason != null) return reason;

    return null; // 全部通过 → 可用
}
```

永旺的 `StoreCouponHelper` 也是同样的模式，只是条件更多。校验顺序也固定：**快速失败 (fail-fast)**，把最容易判断的条件放前面（时间 > 门店 > 渠道），复杂的放后面（商品条件 > 金额门槛）。

---

## 第1层：时间校验

**你已有的版本**：只检查 `expireDate`（日期粒度，只管过期）

**轻量版（CouponMatcher）**：`useStartTime` + `useEndTime` + `expireDate`

**永旺完整版**：
- 使用开始时间 / 结束时间
- 定周期：按月/周/天的指定时间段（如"每周三可用"）
- 自定义时间：领券后 N 天生效，有效 M 天（如"领券后3天生效，有效7天"）

```
你的版本                    轻量版                         永旺完整版
─────────────────          ─────────────────              ─────────────────
expireDate 过期检查         useStartTime 未到生效时间       useStartTime / useEndTime
                           useEndTime   已过使用时间       定周期（月/周/天）
                           expireDate   券已过期           自定义（领券后N天生效+有效M天）
                                                          chargedTime + 生效天数 + 有效天数
```

**对应测试**：`time_withinRange`、`time_beforeStart`、`time_afterEnd`、`time_expired`

**对应源码**：`CouponMatcher#checkTime()` (第172行)

---

## 第2层：门店校验

**你已有的版本**：无

**轻量版**：`applicableStores` 白名单

**永旺完整版**：
- 参与门店白名单
- 领券模式区分：`RECEIVE_STORE_AVAILABLE`（仅领取门店可用）vs 多门店通领通用
- 门店维度的库存检查

```
场景                              结果
────────────────────────         ──────
applicableStores 为空             全门店通用 → 通过
当前门店 S002 在白名单 [S001,S002] → 通过
当前门店 S999 不在白名单           → "门店不适用"
```

**对应测试**：`store_allStores`、`store_inWhitelist`、`store_notInWhitelist`

**对应源码**：`CouponMatcher#checkStore()` (第192行)

---

## 第3层：渠道校验

**你已有的版本**：无

**轻量版**：`applicableChannels` 白名单

**永旺完整版**：
- 渠道白名单（APP / POS / MINI_PROGRAM）
- OSK 收银机特殊规则：只能用代金券（VOUCHER），其他券类型不可用

```
场景                              结果
────────────────────────         ──────
applicableChannels 为空           全渠道通用 → 通过
APP专属券 + APP下单               → 通过
APP专属券 + POS下单               → "渠道不适用"
```

**对应测试**：`channel_allChannels`、`channel_match`、`channel_notMatch`

**对应源码**：`CouponMatcher#checkChannel()` (第205行)

---

## 第4层：商品条件校验（最复杂的一层）

**你已有的版本**：`applicableSkuCodes` SKU 白名单

**轻量版**：全场券(剔除) + 指定商品券(品牌/分类/SKU)

**永旺完整版**：在轻量版基础上增加供应商(supplier)维度

这一层分两种模式：

### 模式A：全场券 + 剔除

券默认适用所有商品，但可以剔除指定的品牌或分类。

```
购物车:
  苹果   → 品牌A / 食品分类 / 60元
  洗衣液 → 品牌B / 日化分类 / 40元
  牛肉   → 品牌C / 食品分类 / 120元

全场券 + 剔除品牌B:
  苹果 ✓  洗衣液 ✗(品牌B被剔除)  牛肉 ✓
  适用金额 = 60 + 120 = 180元
```

**对应测试**：`fullAudience_noExcept`、`fullAudience_exceptBrand`、`fullAudience_exceptCategory`、`fullAudience_allExcepted`

### 模式B：指定商品券

券只适用于指定的 SKU / 品牌 / 分类，三者任一命中即可。

```
指定食品分类券:
  苹果 ✓(食品)  洗衣液 ✗(日化)  牛肉 ✓(食品)
  适用金额 = 60 + 120 = 180元

指定SKU-BEEF券:
  苹果 ✗  洗衣液 ✗  牛肉 ✓
  适用金额 = 120元
```

匹配优先级：SKU精确匹配 > 品牌匹配 > 分类匹配

**对应测试**：`designated_bySku`、`designated_byBrand`、`designated_byCategory`、`designated_noMatch`

**对应源码**：`CouponMatcher#filterMatchedItems()` (第261行)、`isExcepted()` (第276行)、`isDesignated()` (第290行)

---

## 第5层：金额门槛校验

**你已有的版本**：`threshold` 基于购物车总价判断

**轻量版和永旺完整版**：基于**适用商品的金额之和**判断，不是购物车总价

这是你原来版本和真实系统的**关键区别**。

```
购物车: 苹果60 + 洗衣液40 + 牛肉120 = 总价220元

场景: 食品分类券, 满200减50

你的版本:   220 >= 200 → 通过 ✓  ← 错误！洗衣液不是食品，不应该算
轻量版/永旺: 食品金额 = 60+120 = 180 < 200 → "门槛不足" ✗  ← 正确
```

再看一个更隐蔽的场景：

```
场景: 全场券满200, 但剔除品牌C(牛肉)

你的版本:   220 >= 200 → 通过 ✓  ← 错误！牛肉被剔除了
轻量版/永旺: 剔除牛肉后 = 60+40 = 100 < 200 → "门槛不足" ✗  ← 正确
```

**核心原则：先过滤商品（第4层），再算金额（第5层）。**

**对应测试**：`threshold_fullAudienceMet`、`threshold_designatedNotMet`、`threshold_fullAudienceExceptThenNotMet`

**对应源码**：`CouponMatcher#checkThreshold()` (第240行)

---

## 完整校验链的 fail-fast 行为

校验链是短路的——第一个失败的条件直接返回，不继续检查后面的条件。

```
场景: 券未到使用时间 + 门店也不匹配

check() 执行:
  checkTime()    → "未到使用时间" → 直接返回，不走 checkStore()
```

这样设计的好处：
1. **性能**：避免不必要的计算（商品条件校验是最重的）
2. **用户体验**：返回最优先的不可用原因，不会显示"门店不适用"但实际上时间也没到

**对应测试**：`check_failFastOnTime`、`check_failOnStore`

---

## 三个版本的对比总览

| 层 | 你的 isAvailable | 轻量版 CouponMatcher | 永旺 StoreCouponHelper |
|----|-----------------|---------------------|----------------------|
| 1. 时间 | expireDate | start + end + expire | start + end + 定周期 + 自定义 |
| 2. 门店 | - | 白名单 | 白名单 + 领取模式 + 库存 |
| 3. 渠道 | - | 白名单 | 白名单 + OSK特殊规则 |
| 4. 商品 | SKU白名单 | 全场剔除 + 指定(SKU/品牌/分类) | + 供应商维度 |
| 5. 门槛 | 总价 >= threshold | **适用商品金额** >= threshold | 同左 |
| 返回值 | boolean | 不可用原因(String) | 不可用原因 + 状态码 |

---

## 文件路径

```
源码:
  src/main/java/com/bruce/coupondemo/engine/CouponMatcher.java    ← 匹配器（5层校验链）

测试:
  src/test/java/com/bruce/coupondemo/CouponMatcherTest.java        ← 25个测试

永旺真实代码（只读参考）:
  AEON/be-center-coupon/.../StoreCouponHelper.java                 ← 校验方法
  AEON/be-center-coupon/.../StoreCalcCouponLogic.java              ← 计算主逻辑
  AEON/be-center-coupon/.../StoreCouponJudgmentLogic.java          ← 判断逻辑
  AEON/be-center-coupon/.../StoreCouponCondLogic.java              ← 条件获取
```

作者：claude code
