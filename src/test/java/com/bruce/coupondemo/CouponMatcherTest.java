package com.bruce.coupondemo;

import com.bruce.coupondemo.engine.CouponMatcher;
import com.bruce.coupondemo.engine.CouponMatcher.CouponTemplate;
import com.bruce.coupondemo.engine.CouponMatcher.MatchCartItem;
import com.bruce.coupondemo.engine.CouponMatcher.MatchContext;
import com.bruce.coupondemo.model.Coupon;
import com.bruce.coupondemo.model.CouponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 券匹配条件 逐层学习测试
 * <p>
 * 对标永旺 StoreCouponHelper 的校验链，每个测试方法对应一个匹配维度。
 * <p>
 * 永旺校验顺序:
 * 1. 时间 → 2. 门店 → 3. 渠道 → 4. 商品条件(品牌/分类/SKU+剔除) → 5. 金额门槛
 * <p>
 * 标准购物车:
 * - 苹果 SKU-APPLE / 品牌A / 食品分类 / 60元
 * - 洗衣液 SKU-DETERGENT / 品牌B / 日化分类 / 40元
 * - 牛肉 SKU-BEEF / 品牌C / 食品分类 / 120元
 * 总计 220元, 其中食品180元, 日化40元
 *
 * @author claude code
 */
class CouponMatcherTest {

    private CouponMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new CouponMatcher();
    }

    // ======================== 辅助方法 ========================

    private Coupon baseCoupon(String id, BigDecimal faceValue) {
        return Coupon.builder()
                .couponId(id).name(id)
                .couponType(CouponType.PRODUCT)
                .faceValue(faceValue)
                .expireDate(LocalDate.of(2026, 12, 31))
                .build();
    }

    private CouponTemplate baseTemplate(String id, BigDecimal faceValue) {
        return new CouponTemplate(baseCoupon(id, faceValue));
    }

    private List<MatchCartItem> standardCart() {
        return Arrays.asList(
                new MatchCartItem("SKU-APPLE", "BRAND-A", "CAT-FOOD", new BigDecimal("60")),
                new MatchCartItem("SKU-DETERGENT", "BRAND-B", "CAT-DAILY", new BigDecimal("40")),
                new MatchCartItem("SKU-BEEF", "BRAND-C", "CAT-FOOD", new BigDecimal("120"))
        );
    }

    private MatchContext standardContext() {
        return new MatchContext("S001", "APP", LocalDateTime.now(), standardCart());
    }

    // ==================== 第1层: 时间校验 ====================

    @Test
    @DisplayName("时间-1: 券在有效期内 → 通过")
    void time_withinRange() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setUseStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        tpl.setUseEndTime(LocalDateTime.of(2027, 12, 31, 23, 59));

        assertNull(matcher.checkTime(tpl, LocalDateTime.now()));
    }

    @Test
    @DisplayName("时间-2: 未到使用时间 → 不可用")
    void time_beforeStart() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setUseStartTime(LocalDateTime.of(2027, 1, 1, 0, 0));

        assertEquals("未到使用时间", matcher.checkTime(tpl, LocalDateTime.now()));
    }

    @Test
    @DisplayName("时间-3: 已过使用时间 → 不可用")
    void time_afterEnd() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setUseEndTime(LocalDateTime.of(2024, 12, 31, 23, 59));

        assertEquals("已过使用时间", matcher.checkTime(tpl, LocalDateTime.now()));
    }

    @Test
    @DisplayName("时间-4: 券expireDate已过 → 不可用")
    void time_expired() {
        Coupon coupon = baseCoupon("C1", new BigDecimal("50"));
        coupon.setExpireDate(LocalDate.of(2024, 1, 1));
        CouponTemplate tpl = new CouponTemplate(coupon);

        assertEquals("券已过期", matcher.checkTime(tpl, LocalDateTime.now()));
    }

    // ==================== 第2层: 门店校验 ====================

    @Test
    @DisplayName("门店-1: applicableStores为空 → 全门店通用")
    void store_allStores() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));

        assertNull(matcher.checkStore(tpl, "S001"));
    }

    @Test
    @DisplayName("门店-2: 当前门店在白名单 → 通过")
    void store_inWhitelist() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setApplicableStores(new HashSet<>(Arrays.asList("S001", "S002", "S003")));

        assertNull(matcher.checkStore(tpl, "S002"));
    }

    @Test
    @DisplayName("门店-3: 当前门店不在白名单 → 不可用")
    void store_notInWhitelist() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setApplicableStores(new HashSet<>(Arrays.asList("S001", "S002")));

        assertEquals("门店不适用", matcher.checkStore(tpl, "S999"));
    }

    // ==================== 第3层: 渠道校验 ====================

    @Test
    @DisplayName("渠道-1: applicableChannels为空 → 全渠道通用")
    void channel_allChannels() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));

        assertNull(matcher.checkChannel(tpl, "POS"));
    }

    @Test
    @DisplayName("渠道-2: APP专属券, APP下单 → 通过")
    void channel_match() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setApplicableChannels(new HashSet<>(Collections.singletonList("APP")));

        assertNull(matcher.checkChannel(tpl, "APP"));
    }

    @Test
    @DisplayName("渠道-3: APP专属券, POS下单 → 不可用")
    void channel_notMatch() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setApplicableChannels(new HashSet<>(Collections.singletonList("APP")));

        assertEquals("渠道不适用", matcher.checkChannel(tpl, "POS"));
    }

    // ==================== 第4层: 商品条件 - 全场券+剔除 ====================

    @Test
    @DisplayName("全场券-1: 无剔除 → 所有商品(3件220元)都适用")
    void fullAudience_noExcept() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(true);

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals(3, matched.size());
    }

    @Test
    @DisplayName("全场券-2: 剔除品牌B → 洗衣液被剔除, 剩余苹果+牛肉")
    void fullAudience_exceptBrand() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(true);
        tpl.setExceptBrands(new HashSet<>(Collections.singletonList("BRAND-B")));

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals(2, matched.size());
        assertTrue(matched.stream().noneMatch(i -> "SKU-DETERGENT".equals(i.getSkuCode())));
    }

    @Test
    @DisplayName("全场券-3: 剔除日化分类 → 洗衣液被剔除, 剩余食品类")
    void fullAudience_exceptCategory() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(true);
        tpl.setExceptCategories(new HashSet<>(Collections.singletonList("CAT-DAILY")));

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals(2, matched.size());
        assertTrue(matched.stream().allMatch(i -> "CAT-FOOD".equals(i.getCategoryCode())));
    }

    @Test
    @DisplayName("全场券-4: 剔除所有品牌 → 全部被剔除 → 不可用")
    void fullAudience_allExcepted() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(true);
        tpl.setExceptBrands(new HashSet<>(Arrays.asList("BRAND-A", "BRAND-B", "BRAND-C")));

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals("所有商品均被剔除", matcher.checkProductCondition(tpl, matched, standardCart()));
    }

    // ==================== 第4层: 商品条件 - 指定商品券 ====================

    @Test
    @DisplayName("指定券-1: 指定SKU-BEEF → 只匹配牛肉")
    void designated_bySku() {
        Coupon coupon = baseCoupon("C1", new BigDecimal("50"));
        coupon.setApplicableSkuCodes(new HashSet<>(Collections.singletonList("SKU-BEEF")));
        CouponTemplate tpl = new CouponTemplate(coupon);
        tpl.setFullAudience(false);

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals(1, matched.size());
        assertEquals("SKU-BEEF", matched.get(0).getSkuCode());
    }

    @Test
    @DisplayName("指定券-2: 指定品牌A → 只匹配苹果")
    void designated_byBrand() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(false);
        tpl.setApplicableBrands(new HashSet<>(Collections.singletonList("BRAND-A")));

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals(1, matched.size());
        assertEquals("SKU-APPLE", matched.get(0).getSkuCode());
    }

    @Test
    @DisplayName("指定券-3: 指定食品分类 → 匹配苹果+牛肉(2件)")
    void designated_byCategory() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(false);
        tpl.setApplicableCategories(new HashSet<>(Collections.singletonList("CAT-FOOD")));

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals(2, matched.size());
    }

    @Test
    @DisplayName("指定券-4: 指定不存在的品牌X → 无匹配商品 → 不可用")
    void designated_noMatch() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setFullAudience(false);
        tpl.setApplicableBrands(new HashSet<>(Collections.singletonList("BRAND-X")));

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertEquals("无适用商品", matcher.checkProductCondition(tpl, matched, standardCart()));
    }

    // ==================== 第5层: 金额门槛 ====================

    @Test
    @DisplayName("门槛-1: 全场券满200, 全部商品220 → 通过")
    void threshold_fullAudienceMet() {
        Coupon coupon = baseCoupon("C1", new BigDecimal("50"));
        coupon.setThreshold(new BigDecimal("200"));
        CouponTemplate tpl = new CouponTemplate(coupon);

        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        assertNull(matcher.checkThreshold(tpl, matched));
    }

    @Test
    @DisplayName("门槛-2: 指定食品分类满200, 食品金额180(苹果60+牛肉120) → 不足")
    void threshold_designatedNotMet() {
        Coupon coupon = baseCoupon("C1", new BigDecimal("50"));
        coupon.setThreshold(new BigDecimal("200"));
        CouponTemplate tpl = new CouponTemplate(coupon);
        tpl.setFullAudience(false);
        tpl.setApplicableCategories(new HashSet<>(Collections.singletonList("CAT-FOOD")));

        // 食品: 苹果60 + 牛肉120 = 180 < 200（洗衣液40是日化，不计入）
        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        String reason = matcher.checkThreshold(tpl, matched);
        assertNotNull(reason);
        assertTrue(reason.contains("180"));
        assertTrue(reason.contains("200"));
    }

    @Test
    @DisplayName("门槛-3: 全场券满200, 剔除品牌C(牛肉120) → 剩余100 → 不足")
    void threshold_fullAudienceExceptThenNotMet() {
        Coupon coupon = baseCoupon("C1", new BigDecimal("50"));
        coupon.setThreshold(new BigDecimal("200"));
        CouponTemplate tpl = new CouponTemplate(coupon);
        tpl.setFullAudience(true);
        tpl.setExceptBrands(new HashSet<>(Collections.singletonList("BRAND-C")));

        // 剔除牛肉(BRAND-C=120) → 苹果60 + 洗衣液40 = 100 < 200
        List<MatchCartItem> matched = matcher.filterMatchedItems(tpl, standardCart());
        String reason = matcher.checkThreshold(tpl, matched);
        assertNotNull(reason);
        assertTrue(reason.contains("100"));
    }

    // ==================== 完整校验链 ====================

    @Test
    @DisplayName("校验链-1: 所有条件都满足 → 可用(返回null)")
    void check_allPass() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        assertNull(matcher.check(tpl, standardContext()));
    }

    @Test
    @DisplayName("校验链-2: 时间不满足 → 直接返回, 不检查后续门店/渠道/商品")
    void check_failFastOnTime() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setUseStartTime(LocalDateTime.of(2099, 1, 1, 0, 0));
        tpl.setApplicableStores(new HashSet<>(Collections.singletonList("S999")));

        // 时间先命中，不走到门店
        assertEquals("未到使用时间", matcher.check(tpl, standardContext()));
    }

    @Test
    @DisplayName("校验链-3: 时间OK + 门店不匹配 → 返回门店原因")
    void check_failOnStore() {
        CouponTemplate tpl = baseTemplate("C1", new BigDecimal("50"));
        tpl.setApplicableStores(new HashSet<>(Collections.singletonList("S999")));

        assertEquals("门店不适用", matcher.check(tpl, standardContext()));
    }

    @Test
    @DisplayName("校验链-4: 综合场景 - 指定食品满200, 时间/门店/渠道都OK, 但食品金额180 → 门槛不足")
    void check_complexThresholdFail() {
        Coupon coupon = baseCoupon("C1", new BigDecimal("50"));
        coupon.setThreshold(new BigDecimal("200"));
        CouponTemplate tpl = new CouponTemplate(coupon);
        tpl.setFullAudience(false);
        tpl.setApplicableCategories(new HashSet<>(Collections.singletonList("CAT-FOOD")));
        tpl.setApplicableStores(new HashSet<>(Collections.singletonList("S001")));
        tpl.setApplicableChannels(new HashSet<>(Collections.singletonList("APP")));

        String reason = matcher.check(tpl, standardContext());
        assertNotNull(reason);
        assertTrue(reason.contains("金额不足"));
    }
}
