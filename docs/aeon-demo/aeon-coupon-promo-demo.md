# AEON(永旺)「促销 + 优惠券」最小化可运行 Demo

本 Demo 目标：帮你把《永旺券&促销.md》里的**计算顺序**和**关键约束**跑一遍，做到面试能讲清楚“为什么要先促销后用券、为什么要运费二次计算、券怎么推荐”的核心逻辑即可（不追求 100% 还原生产细节）。

## 1. 对齐 AEON 的核心流程（先促销，后用券）

整体链路（对应《永旺券&促销.md》）：

1. **促销计算（先）**：输出每个商品行的促销后价格/净额
2. **商品券计算（后）**：输入使用促销后金额（promoAmount / promoPrice），推荐勾选商品券
3. **运费二次计算**：商品券把商品实付改了 → 运费阶梯可能变化
4. **运费券重算**：基于新的运费金额，重新推荐运费券
5. 汇总出最终应付金额

本仓库实现位置（均为新增代码，不改原有业务代码）：

- 启动类：`src/main/java/com/aeon/demo/AeonDemoApplication.java`
- 编排服务：`src/main/java/com/aeon/demo/service/AeonOrderCalcService.java`
- 促销引擎：`src/main/java/com/aeon/demo/engine/promo/PromoCalcEngine.java`
- 券引擎：`src/main/java/com/aeon/demo/engine/coupon/CouponCalcEngine.java`
- 接口：`src/main/java/com/aeon/demo/controller/AeonOrderCalcController.java`

## 2. 怎么跑（单测 + 接口）

### 2.1 跑单测（推荐你先跑这个）

只跑本 Demo 新增测试：

```bash
mvn -q -Dtest=AeonOrderCalcFlowTest,AeonOrderCalcControllerTest test
```

### 2.2 启动接口（Postman）

启动 Demo（端口固定 19999；通过 profile 隔离，不影响工程原有应用）：

```bash
mvn -q -DskipTests spring-boot:run -Dspring-boot.run.mainClass=com.aeon.demo.AeonDemoApplication
```

接口：

- GET 示例入参：`GET http://localhost:19999/aeon-demo/order/sample`
- POST 计算：`POST http://localhost:19999/aeon-demo/order/calc`

#### Postman 入参（直接复制即可）

你也可以先调 sample 接口拿到同样的 JSON。

```json
{
  "scenario": "S1",
  "memberId": "M10001",
  "storeCode": "S001",
  "channel": 1,
  "platform": "APP",
  "logisticFee": null,
  "cartItems": [
    { "cartItemId": "C1", "skuId": "SKU-APPLE", "quantity": 2, "salePrice": 30.00 },
    { "cartItemId": "C2", "skuId": "SKU-BEEF",  "quantity": 1, "salePrice": 120.00 },
    { "cartItemId": "C3", "skuId": "SKU-MILK",  "quantity": 3, "salePrice": 20.00 }
  ],
  "coupons": []
}
```

说明：

- `scenario=S1`：使用内置促销/券/互斥组/运费阶梯（在 `AeonScenarioFactory` 里定义）
- `coupons=[]`：表示不自定义券列表，直接用场景内置券
- `logisticFee=null`：表示用券前运费也由场景规则计算（先算一次运费，后面还会二次重算）

## 3. 你能在返回里看到哪些“关键中间态”

响应会把关键中间结果都返回（便于你画流程图/讲解）：

- `promo`：促销结果（包含每行商品 `promoAmount`，即“商品净额”）
- `goodsCoupons`：商品券推荐结果（勾选/不可用原因）
- `shippingCouponsBeforeFreightRecalc`：用券前运费下的运费券推荐（通常运费=0时都不可用）
- `shippingCouponsAfterFreightRecalc`：运费二次计算后的运费券推荐（最终以这个为准）
- `freight`：运费二次计算前/后
- `amountSummary`：汇总金额
- `trace`：按步骤输出一段可读的流程说明（面试时你可以照着讲）

S1 场景的关键数字（便于你核对）：

- 原价商品总额：240.00
- 促销后商品总额：200.00
- 商品券优惠：145.00 → 商品实付：55.00
- 运费（用券前）：0.00（>=199 免运费）
- 运费（用券后二次）：12.00（<199 收 12）
- 运费券优惠（重算后）：11.00 → 运费实付：1.00
- 最终应付：56.00

## 4. 面试/简历怎么描述（示例话术）

你可以按这个结构讲（抓住“顺序”和“为什么”）：

1. **订单确认阶段的计算顺序**：先促销后用券；券必须基于促销后金额计算（否则会重复优惠或口径不一致）。
2. **促销取低价策略**：商品命中多个促销时，需要决定“每个商品到底用哪个促销”。组合促销使用“笛卡尔积穷举所有分配方案”，逐方案计算总优惠，取最低价方案；商品少时可穷举，商品多时退化为单品取低/贪心。
3. **券推荐策略（贪心）**：商品券/运费券拆开算；按“面值高、范围窄、先过期、门槛高”等规则排序，逐张尝试加入，校验互斥组、叠加限制、同模板上限、金额溢出，输出推荐勾选方案。
4. **运费二次计算与运费券重算**：商品券会改变商品实付，运费阶梯可能变化，因此需要重算运费；运费变了后，运费券可用性/推荐结果也要跟着重算，保证最终金额一致。

---

作者：codex

