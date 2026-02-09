# 券+促销联动方案 对照学习指南

## 怎么用这个文档

这个项目里有两套实现：

| 包 | 定位 | 核心文件 |
|---|------|---------|
| `com.bruce.coupondemo` | 最小方案，聚焦核心流程 | `CouponEngine`, `OrderCalcService`, 6个测试 |
| `com.aeon.demo` | 对齐永旺真实业务 | `PromoCalcEngine`, `CouponCalcEngine`, `AeonOrderCalcService` |

建议顺序：先跑通 coupondemo 的测试理解主线，再读 aeon.demo 理解真实业务的复杂度，最后动手补齐 coupondemo 缺失的部分。

---

## 第一步：跑通 coupondemo 的 6 个测试

```bash
mvn test -Dtest=com.bruce.coupondemo.PromotionCouponIntegrationTest
```

6 个场景对应 6 个核心知识点，逐个阅读：

### 场景1：纯促销（理解促销引擎）

**文件**: `PromotionCouponIntegrationTest#testPurePromotion`

```
输入: 200元, 满200减30
输出: 总付170
```

**要点**: 促销引擎 `NewPromotionEngine` 是黑盒引用，不需要自己写。重点理解它的接口：
```java
Cart result = engine.calculate(cart, rules);
result.getTotalDiscount();  // 优惠金额
result.getPayPrice();       // 应付金额
```

**动手**: 打开 `com.bruce.promotiondemo.engine.NewPromotionEngine`，阅读 `calculate` 方法，理解穷举最优的实现。

### 场景2：纯用券（理解券引擎）

**文件**: `PromotionCouponIntegrationTest#testPureCoupon`

```
输入: 200元, 50元商品券 + 10元运费券, 运费15
输出: 商品付150, 运费付5, 总付155
```

**要点**: 券引擎将商品券和运费券分开计算，各自独立贪心。

**动手**: 打开 `com.bruce.coupondemo.engine.CouponEngine`，跟踪 `calculate` 方法的流程：
1. `isAvailable()` — 过滤（过期、门槛、SKU）
2. 按类型分组（PRODUCT / SHIPPING）
3. `greedySelect()` — 排序后逐张选择

### 场景3：促销+券联动（理解编排顺序）

**文件**: `PromotionCouponIntegrationTest#testPromotionAndCoupon`

```
输入: 250元, 满200减30, 50元券(满150), 运费15, 10元运费券
流程: 250 →(促销)→ 220 →(券基于220判断门槛)→ 220-50=170, 运费15-10=5
输出: 总付175
```

**要点**: 这是核心——券的门槛判断基于促销后价格，不是原价。编排顺序写在 `OrderCalcService#calculate` 里：

```java
// Step 1: 促销
Cart promotedCart = promotionEngine.calculate(cart, rules);
BigDecimal priceAfterPromotion = promotedCart.getPayPrice();

// Step 2: 券（基于促销后价格）
CouponResult couponResult = couponEngine.calculate(priceAfterPromotion, ...);
```

### 场景4：促销后券门槛不满足（架构价值点）

**文件**: `PromotionCouponIntegrationTest#testCouponThresholdNotMetAfterPromotion`

```
输入: 200元, 8折→160, 50元券(满180)
关键: 原价200满足门槛180，但促销后160不满足
输出: 券不可用，总付160
```

**这是整个方案存在的理由**。如果券基于原价判断门槛，用户会觉得券能用但实际不应该用。"先促销后券"保证了门槛判断的正确性。

**动手**: 在 `CouponEngine#isAvailable` 里找到门槛检查代码，确认它用的是 `priceAfterPromotion` 而不是原价。

### 场景5：券互斥（理解互斥组）

**文件**: `PromotionCouponIntegrationTest#testCouponExclusiveGroup`

```
输入: 300元, 50元券(G1) + 30元券(G1) + 20元券(无组)
流程: 50元选中 → 30元同组G1跳过 → 20元选中
输出: 优惠70，总付230
```

**动手**: 在 `CouponEngine#greedySelect` 里找到 `usedExclusiveGroups` 的检查逻辑。

### 场景6：券溢出检测（理解截断逻辑）

**文件**: `PromotionCouponIntegrationTest#testCouponOverflow`

```
输入: 30元商品, 50元券(无门槛)
关键: 面值50 > 商品价30
输出: 实际优惠30(截断), 总付0
```

**动手**: 在 `greedySelect` 里找到 `BigDecimal actualDiscount = coupon.getFaceValue().min(currentRemain)` 这行。

---

## 第二步：阅读 aeon.demo 理解真实业务复杂度

先跑通 aeon 的测试：

```bash
mvn test -Dtest=com.aeon.demo.AeonOrderCalcFlowTest
```

### 核心差异：运费二次计算

这是 coupondemo 没有建模的关键环节。打开 `AeonOrderCalcService#calc`（约第 80-110 行），看这段流程：

```
原价 240
  ↓ 促销
促销后 200
  ↓ 运费计算（第一次）
200 >= 199 → 免运费 = 0
  ↓ 商品券
商品券优惠 145 → 商品实付 55
  ↓ 运费计算（第二次）   ← 关键！
55 < 199 → 运费 = 12    ← 运费从0变成12了！
  ↓ 运费券
运费券优惠 11 → 运费实付 1
  ↓
最终 55 + 1 = 56
```

**业务含义**: 用户用了大额商品券后，商品金额降到了免运费门槛以下，运费重新出现了。这在电商场景中是真实存在的问题。

### 对照阅读清单

按以下顺序读 aeon.demo 的源码：

| 顺序 | 文件 | 重点关注 |
|------|------|---------|
| 1 | `scenario/AeonScenarioFactory.java` | S1 场景的数据配置：3个促销、9张券、2个互斥组 |
| 2 | `engine/promo/PromoCalcEngine.java` | 笛卡尔积穷举 + 组合促销分摊（对比 `NewPromotionEngine` 的穷举） |
| 3 | `engine/coupon/CouponCalcEngine.java` | 商品券按 SKU 计算适用金额 + 跨类型互斥 + 同模板叠加上限 |
| 4 | `engine/freight/StepFreightCalculator.java` | 阶梯运费（最简单，2分钟读完） |
| 5 | `service/AeonOrderCalcService.java` | 编排主流程，重点看第二次运费计算的触发位置 |
| 6 | `util/MoneyAllocator.java` | 按权重分摊金额，理解尾差处理 |

---

## 第三步：动手补齐 coupondemo 的缺失

以下是 coupondemo 目前缺少但 aeon.demo 已实现的功能点。建议按顺序逐个补齐。

### 练习1：运费二次计算

**目标**: 用完商品券后，根据新的商品实付金额重新计算运费。

需要修改的文件：
- `OrderCalcService#calculate` — 在商品券计算后，加入运费重算逻辑
- `OrchestrationResult` — 增加 `freightBeforeCoupon` 和 `freightAfterCoupon` 字段

新建的文件：
- `engine/FreightCalculator.java` — 阶梯运费计算（参考 `StepFreightCalculator`）

新增测试场景：
```
场景7: 用券后运费翻转
输入: 250元, 满200减30→220, 100元券, 免运费门槛199, 基础运费12
流程: 促销后220(>=199免运费=0) → 用券后120(<199运费=12)
期望: freightBefore=0, freightAfter=12, 最终=120+12=132
```

```
场景8: 用券后仍然免运费
输入: 300元, 50元券, 免运费门槛199, 基础运费12
流程: 300(>=199免运费=0) → 用券后250(>=199仍免运费=0)
期望: freightBefore=0, freightAfter=0, 最终=250
```

### 练习2：运费券基于二次运费重算

**目标**: 运费券的门槛判断和抵扣基于二次计算后的运费，不是原始运费。

这个紧跟练习1，修改 `OrderCalcService` 中运费券的计算入参：

```java
// 之前：运费券基于原始运费
couponEngine.calculate(priceAfterPromotion, shippingFee, coupons, items);

// 之后：先算商品券，再算新运费，运费券基于新运费
BigDecimal freightAfter = freightCalculator.calc(productPayPrice);
couponEngine.calculateShipping(freightAfter, shippingCoupons);
```

新增测试场景：
```
场景9: 运费二次计算后运费券不可用
输入: 250元, 100元券, 10元运费券(满10可用), 免运费门槛199, 基础运费12
流程: 用券后150(<199运费=12) → 运费券可用 → 运费实付2
期望: 总付152

场景10: 运费二次计算后运费为0，运费券全部不可用
输入: 300元, 50元券, 10元运费券, 免运费门槛199, 基础运费12
流程: 用券后250(>=199免运费=0) → 运费券不可用
期望: 运费0, 运费券优惠0, 总付250
```

### 练习3：跨类型券互斥

**目标**: 商品券和运费券之间也可以互斥（如：选了商品券A，运费券B就不能用）。

需要修改的文件：
- `model/Coupon.java` — `exclusiveGroupId` 改为支持跨类型（或者改用模板ID+互斥策略类）
- `engine/CouponEngine.java` — 商品券选完后，把已选的互斥信息传递给运费券计算

参考 aeon.demo 的做法：
- `MutuallyExclusivePolicy` — 互斥组的倒排索引
- `CouponCalcEngine#calcShippingCoupons` 的 `selectedGoodsTemplateIds` 参数

### 练习4：同模板叠加上限

**目标**: 同一个券模板最多选 N 张（默认1张）。

需要修改的文件：
- `model/Coupon.java` — 增加 `couponTemplateId` 和 `sameTemplateUseLimit` 字段
- `engine/CouponEngine#greedySelect` — 增加模板计数检查

---

## 第四步：测试写法的注意事项

以下是从两套实现中总结的测试最佳实践：

### 做

- **一个测试方法只验证一个关注点**。场景4只验证"促销后门槛不满足"，不掺杂其他断言
- **用 `compareTo` 比较 BigDecimal**。`assertEquals(0, expected.compareTo(actual))` 避免 scale 不同导致误判
- **输入数据自包含在测试方法内**。不要依赖外部工厂类，读测试的人不需要跳转到别的文件
- **纯单元测试优先**。能 `new` 就别用 `@SpringBootTest`

### 不做

- 不要在一个测试方法里堆 10+ 个断言覆盖所有环节
- 不要用 `System.out.println` 做性能统计（单次运行包含 JVM 预热/类加载/GC 抖动，结论不可信）
- 不要对贪心算法的具体选券结果做过于精确的断言（如断言具体券ID排列），除非你明确要锁定排序规则
- BigDecimal 不要用 `assertEquals` 直接比较（`new BigDecimal("30")` != `new BigDecimal("30.00")`）

### （可选）怎么做“靠谱的耗时评估”（推荐：JMH）

如果你想评估“纯计算引擎”的耗时（例如：`PromoCalcEngine` 的穷举/`CouponCalcEngine` 的贪心），**建议用 JMH**，而不是在测试里 `System.out.println(System.currentTimeMillis())` 这种手动计时。

JMH 的好处：

- 自带预热（Warmup）与多次测量（Measurement），避免第一次运行的噪声
- 支持 Fork/线程模型/统计输出，更接近可复现的结论
- 你能在简历/面试里更专业地表达“我做过基准测试验证性能边界”

最小示例（只测“编排服务一次计算”的平均耗时；你也可以改成测促销/券引擎单独方法）：

```java
import com.aeon.demo.dto.AeonOrderCalcRequest;
import com.aeon.demo.dto.CartItemRequest;
import com.aeon.demo.service.AeonOrderCalcService;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class AeonOrderCalcBenchmark {

    private AeonOrderCalcService service;
    private AeonOrderCalcRequest req;

    @Setup(Level.Trial)
    public void setup() {
        service = new AeonOrderCalcService(); // 纯计算对象，不需要 Spring

        CartItemRequest apple = new CartItemRequest();
        apple.setCartItemId("C1");
        apple.setSkuId("SKU-APPLE");
        apple.setQuantity(2);
        apple.setSalePrice(new BigDecimal("30.00"));

        CartItemRequest beef = new CartItemRequest();
        beef.setCartItemId("C2");
        beef.setSkuId("SKU-BEEF");
        beef.setQuantity(1);
        beef.setSalePrice(new BigDecimal("120.00"));

        CartItemRequest milk = new CartItemRequest();
        milk.setCartItemId("C3");
        milk.setSkuId("SKU-MILK");
        milk.setQuantity(3);
        milk.setSalePrice(new BigDecimal("20.00"));

        req = new AeonOrderCalcRequest();
        req.setScenario("S1");
        req.setCartItems(Arrays.asList(apple, beef, milk));
    }

    @Benchmark
    public Object calc() {
        return service.calc(req);
    }
}
```

落地建议（不一定要现在做，但你知道正确姿势）：

- Benchmark 只测“纯计算方法”，不要把 `@SpringBootTest` 的上下文启动时间算进去
- 如果要测“算法复杂度边界”，建议构造不同购物车规模/不同可选促销数量的输入，用 JMH 跑对比曲线

---

## 快速参考：文件路径

```
coupondemo（你的最小方案）:
  src/main/java/com/bruce/coupondemo/
    model/          CouponType, Coupon, CouponResult, OrchestrationResult
    engine/         CouponEngine
    service/        OrderCalcService
    controller/     CouponDemoController
  src/test/java/com/bruce/coupondemo/
    PromotionCouponIntegrationTest    ← 6个场景

aeon.demo（对标永旺真实业务）:
  src/main/java/com/aeon/demo/
    domain/         Promotion, Coupon, CartItem, 枚举
    dto/            Request/Response, AmountSummary, FreightInfo
    engine/promo/   PromoCalcEngine              ← 促销穷举
    engine/coupon/  CouponCalcEngine             ← 券贪心（含跨类互斥）
    engine/freight/ StepFreightCalculator        ← 阶梯运费
    scenario/       AeonScenarioFactory          ← S1场景数据
    service/        AeonOrderCalcService         ← 编排主流程
    util/           MoneyAllocator, MoneyUtils   ← 金额分摊
  src/test/java/com/aeon/demo/
    AeonOrderCalcFlowTest             ← 1个综合场景

promotiondemo（促销引擎，只引用不修改）:
  src/main/java/com/bruce/promotiondemo/
    engine/         NewPromotionEngine           ← 穷举最优
    model/          Cart, CartItem, Rule, RuleType
```

作者：claude code
