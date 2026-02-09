package com.bruce.coupondemo.engine;

import com.bruce.coupondemo.model.Coupon;
import com.bruce.coupondemo.model.CouponType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 券匹配器 - 判断一张券是否可用，并返回不可用原因
 * <p>
 * 对标永旺 StoreCouponHelper 的校验链：
 * validatorUseTime → validatorUseStore → validatorUseChannel →
 * validatorCouponOrderItemDTO(商品条件) → validatorGoodsAmount(门槛)
 * <p>
 * 每个检查方法独立，返回 null 表示通过，否则返回不可用原因。
 * 调用方拿到第一个非null原因即可标记该券不可用。
 *
 * @author claude code
 */
public class CouponMatcher {

    /**
     * 匹配上下文 - 代表当前下单环境
     * <p>
     * 永旺实际会从订单/会员/门店服务拿到这些信息，这里简化为一个扁平对象。
     */
    public static class MatchContext {
        /** 当前门店编码 */
        private String storeCode;
        /** 当前渠道: APP / POS / MINI_PROGRAM */
        private String channel;
        /** 当前时间 */
        private LocalDateTime now;
        /** 购物车商品（含品牌、分类信息） */
        private List<MatchCartItem> items;

        public MatchContext(String storeCode, String channel, LocalDateTime now, List<MatchCartItem> items) {
            this.storeCode = storeCode;
            this.channel = channel;
            this.now = now;
            this.items = items;
        }

        public String getStoreCode() { return storeCode; }
        public String getChannel() { return channel; }
        public LocalDateTime getNow() { return now; }
        public List<MatchCartItem> getItems() { return items; }
    }

    /**
     * 购物车商品行 - 带品牌和分类信息
     * <p>
     * 真实系统中品牌/分类是从商品中心查的，这里直接写在商品行上。
     */
    public static class MatchCartItem {
        private String skuCode;
        private String brandCode;
        private String categoryCode;
        /** 该商品行的促销后金额 */
        private BigDecimal amount;

        public MatchCartItem(String skuCode, String brandCode, String categoryCode, BigDecimal amount) {
            this.skuCode = skuCode;
            this.brandCode = brandCode;
            this.categoryCode = categoryCode;
            this.amount = amount;
        }

        public String getSkuCode() { return skuCode; }
        public String getBrandCode() { return brandCode; }
        public String getCategoryCode() { return categoryCode; }
        public BigDecimal getAmount() { return amount; }
    }

    /**
     * 增强的券模板 - 在 Coupon 基础上增加门店/渠道/品牌/分类等匹配维度
     * <p>
     * 真实系统中这些字段在券模板表(coupon_template)上，这里简化为一个对象。
     */
    public static class CouponTemplate {
        private Coupon coupon;
        /** 使用开始时间（null=不限） */
        private LocalDateTime useStartTime;
        /** 使用结束时间（null=不限） */
        private LocalDateTime useEndTime;
        /** 适用门店（null或空=全门店） */
        private Set<String> applicableStores;
        /** 适用渠道（null或空=全渠道） */
        private Set<String> applicableChannels;

        // ========== 商品条件 ==========
        /** 条件类型: true=全场(可有剔除), false=指定商品 */
        private boolean fullAudience;
        /** 指定品牌（fullAudience=false时生效） */
        private Set<String> applicableBrands;
        /** 指定分类（fullAudience=false时生效） */
        private Set<String> applicableCategories;
        /** 剔除品牌（fullAudience=true时生效） */
        private Set<String> exceptBrands;
        /** 剔除分类（fullAudience=true时生效） */
        private Set<String> exceptCategories;

        public CouponTemplate(Coupon coupon) {
            this.coupon = coupon;
            this.fullAudience = true;
        }

        public Coupon getCoupon() { return coupon; }
        public LocalDateTime getUseStartTime() { return useStartTime; }
        public void setUseStartTime(LocalDateTime useStartTime) { this.useStartTime = useStartTime; }
        public LocalDateTime getUseEndTime() { return useEndTime; }
        public void setUseEndTime(LocalDateTime useEndTime) { this.useEndTime = useEndTime; }
        public Set<String> getApplicableStores() { return applicableStores; }
        public void setApplicableStores(Set<String> applicableStores) { this.applicableStores = applicableStores; }
        public Set<String> getApplicableChannels() { return applicableChannels; }
        public void setApplicableChannels(Set<String> applicableChannels) { this.applicableChannels = applicableChannels; }
        public boolean isFullAudience() { return fullAudience; }
        public void setFullAudience(boolean fullAudience) { this.fullAudience = fullAudience; }
        public Set<String> getApplicableBrands() { return applicableBrands; }
        public void setApplicableBrands(Set<String> applicableBrands) { this.applicableBrands = applicableBrands; }
        public Set<String> getApplicableCategories() { return applicableCategories; }
        public void setApplicableCategories(Set<String> applicableCategories) { this.applicableCategories = applicableCategories; }
        public Set<String> getExceptBrands() { return exceptBrands; }
        public void setExceptBrands(Set<String> exceptBrands) { this.exceptBrands = exceptBrands; }
        public Set<String> getExceptCategories() { return exceptCategories; }
        public void setExceptCategories(Set<String> exceptCategories) { this.exceptCategories = exceptCategories; }
    }

    // ======================== 校验链 ========================

    /**
     * 完整校验：依次检查所有条件，返回第一个不满足的原因。
     * 返回 null 表示券可用。
     */
    public String check(CouponTemplate tpl, MatchContext ctx) {
        String reason;

        reason = checkTime(tpl, ctx.getNow());
        if (reason != null) return reason;

        reason = checkStore(tpl, ctx.getStoreCode());
        if (reason != null) return reason;

        reason = checkChannel(tpl, ctx.getChannel());
        if (reason != null) return reason;

        // 找出该券适用的商品行
        List<MatchCartItem> matchedItems = filterMatchedItems(tpl, ctx.getItems());

        reason = checkProductCondition(tpl, matchedItems, ctx.getItems());
        if (reason != null) return reason;

        reason = checkThreshold(tpl, matchedItems);
        if (reason != null) return reason;

        return null; // 全部通过
    }

    // ======================== 各条件独立方法 ========================

    /**
     * 条件1: 使用时间校验
     * <p>
     * 永旺实际还有"定周期"(按月/周/天的指定时间段)和"自定义时间"(领券后N天生效)，
     * 这里简化为 useStartTime ~ useEndTime 区间。
     */
    public String checkTime(CouponTemplate tpl, LocalDateTime now) {
        if (tpl.getUseStartTime() != null && now.isBefore(tpl.getUseStartTime())) {
            return "未到使用时间";
        }
        if (tpl.getUseEndTime() != null && now.isAfter(tpl.getUseEndTime())) {
            return "已过使用时间";
        }
        // 兼容 Coupon 上的 expireDate（日期粒度）
        Coupon c = tpl.getCoupon();
        if (c.getExpireDate() != null && now.toLocalDate().isAfter(c.getExpireDate())) {
            return "券已过期";
        }
        return null;
    }

    /**
     * 条件2: 门店校验
     * <p>
     * 永旺还区分"仅领取门店可用"和"多门店通领通用"两种模式，这里简化为白名单。
     */
    public String checkStore(CouponTemplate tpl, String storeCode) {
        Set<String> stores = tpl.getApplicableStores();
        if (stores != null && !stores.isEmpty() && !stores.contains(storeCode)) {
            return "门店不适用";
        }
        return null;
    }

    /**
     * 条件3: 渠道校验
     * <p>
     * 永旺的OSK收银机渠道只能用代金券，其他渠道按白名单匹配。这里简化为白名单。
     */
    public String checkChannel(CouponTemplate tpl, String channel) {
        Set<String> channels = tpl.getApplicableChannels();
        if (channels != null && !channels.isEmpty() && !channels.contains(channel)) {
            return "渠道不适用";
        }
        return null;
    }

    /**
     * 条件4: 商品条件校验
     * <p>
     * 这是最复杂的一层，分两种模式：
     * - fullAudience=true (全场券): 所有商品都适用，但要剔除指定品牌/分类
     * - fullAudience=false (指定商品券): 只有匹配品牌/分类/SKU的商品才适用
     * <p>
     * 永旺实际还支持按供应商(supplier)匹配，这里省略。
     */
    public String checkProductCondition(CouponTemplate tpl, List<MatchCartItem> matchedItems,
                                        List<MatchCartItem> allItems) {
        if (matchedItems.isEmpty()) {
            if (tpl.isFullAudience()) {
                return "所有商品均被剔除";
            } else {
                return "无适用商品";
            }
        }
        return null;
    }

    /**
     * 条件5: 金额门槛校验
     * <p>
     * 注意：门槛基于"适用商品的金额之和"，不是购物车总金额。
     * 例如：券只适用于"食品"分类，那门槛只看食品的金额。
     */
    public String checkThreshold(CouponTemplate tpl, List<MatchCartItem> matchedItems) {
        Coupon c = tpl.getCoupon();
        if (c.getThreshold() != null && c.getThreshold().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal applicableAmount = matchedItems.stream()
                    .map(MatchCartItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (applicableAmount.compareTo(c.getThreshold()) < 0) {
                return "适用商品金额不足(需" + c.getThreshold() + "元,实际" + applicableAmount + "元)";
            }
        }
        return null;
    }

    // ======================== 商品匹配过滤 ========================

    /**
     * 过滤出券适用的商品行
     * <p>
     * 全场券: 排除剔除品牌/分类后的剩余商品
     * 指定商品券: 匹配指定SKU/品牌/分类的商品
     */
    public List<MatchCartItem> filterMatchedItems(CouponTemplate tpl, List<MatchCartItem> allItems) {
        if (tpl.isFullAudience()) {
            return allItems.stream()
                    .filter(item -> !isExcepted(tpl, item))
                    .collect(Collectors.toList());
        } else {
            return allItems.stream()
                    .filter(item -> isDesignated(tpl, item))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 全场券: 判断商品是否被剔除
     */
    private boolean isExcepted(CouponTemplate tpl, MatchCartItem item) {
        if (tpl.getExceptBrands() != null && tpl.getExceptBrands().contains(item.getBrandCode())) {
            return true;
        }
        if (tpl.getExceptCategories() != null && tpl.getExceptCategories().contains(item.getCategoryCode())) {
            return true;
        }
        return false;
    }

    /**
     * 指定商品券: 判断商品是否在指定范围内
     * SKU精确匹配 > 品牌匹配 > 分类匹配，任一命中即可
     */
    private boolean isDesignated(CouponTemplate tpl, MatchCartItem item) {
        Coupon c = tpl.getCoupon();
        // SKU匹配
        if (c.getApplicableSkuCodes() != null && c.getApplicableSkuCodes().contains(item.getSkuCode())) {
            return true;
        }
        // 品牌匹配
        if (tpl.getApplicableBrands() != null && tpl.getApplicableBrands().contains(item.getBrandCode())) {
            return true;
        }
        // 分类匹配
        if (tpl.getApplicableCategories() != null && tpl.getApplicableCategories().contains(item.getCategoryCode())) {
            return true;
        }
        return false;
    }
}
