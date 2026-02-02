package com.aeon.demo.engine.coupon;

import com.aeon.demo.domain.Coupon;
import com.aeon.demo.domain.CouponCategory;
import com.aeon.demo.domain.CouponConditionType;
import com.aeon.demo.engine.promo.PromoItemResult;
import com.aeon.demo.util.MoneyAllocator;
import com.aeon.demo.util.MoneyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 券计算引擎（最小化）。
 *
 * <p>对齐《永旺券&促销.md》：商品券/运费券分开算，默认“贪心”推荐最优方案。</p>
 *
 * @author codex
 */
public class CouponCalcEngine {

    /**
     * 一笔订单最多使用几种不同模板的券（AEON 默认 3）。
     */
    private final int goodsTemplateUseLimitCount;

    public CouponCalcEngine() {
        this(3);
    }

    public CouponCalcEngine(int goodsTemplateUseLimitCount) {
        this.goodsTemplateUseLimitCount = goodsTemplateUseLimitCount <= 0 ? 3 : goodsTemplateUseLimitCount;
    }

    public CouponCalcResult calcGoodsCoupons(List<PromoItemResult> promoItems,
                                             List<Coupon> allCoupons,
                                             MutuallyExclusivePolicy exclusivePolicy,
                                             LocalDateTime now) {
        List<PromoItemResult> items = promoItems == null ? Collections.emptyList() : promoItems;
        List<Coupon> coupons = allCoupons == null ? Collections.emptyList() : allCoupons;
        MutuallyExclusivePolicy policy = exclusivePolicy == null ? new MutuallyExclusivePolicy(Collections.emptyMap()) : exclusivePolicy;
        LocalDateTime clock = now == null ? LocalDateTime.now() : now;

        CouponCalcResult result = new CouponCalcResult();

        Map<String, ItemAmount> itemAmountMap = buildItemAmountMap(items);

        List<Candidate> available = new ArrayList<>();
        List<CouponView> notAvailable = new ArrayList<>();
        for (Coupon c : coupons) {
            if (c.getCategory() != CouponCategory.GOODS) {
                continue;
            }
            Candidate candidate = buildGoodsCandidate(c, itemAmountMap, clock);
            if (!candidate.eligible) {
                notAvailable.add(candidate.view);
            } else {
                available.add(candidate);
            }
        }

        // 只有一张券可用：直接勾选（允许溢出——最终实付再做 max(0,...)）
        if (available.size() == 1) {
            Candidate only = available.get(0);
            only.view.setChecked(true);
            only.view.setUsableStatus(CouponView.STATUS_USABLE);
            only.view.setUsableStatusDesc("仅一张可用券，默认选中（允许溢出）");

            result.setDiscountAmount(MoneyUtils.scale(only.coupon.getParValue()));
            result.setAvailableCouponList(Collections.singletonList(only.view));
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        // 排序策略（对齐 AEON：面值高 -> 范围窄 -> 适用商品数少 -> 先过期 -> 门槛高）
        available.sort(Comparator
                .comparing((Candidate c) -> MoneyUtils.scale(c.coupon.getParValue())).reversed()
                .thenComparing((Candidate c) -> c.view.getConditionType(), Comparator.reverseOrder())
                .thenComparing(c -> c.view.getApplicableCartItemIds().size())
                .thenComparing(c -> c.coupon.getUseEndTime() == null ? LocalDateTime.MAX : c.coupon.getUseEndTime())
                .thenComparing((Candidate c) -> MoneyUtils.scale(c.coupon.getBound()), Comparator.reverseOrder()));

        // 单张大于适用商品金额 / 小于等于
        List<Candidate> greater = available.stream()
                .filter(c -> MoneyUtils.scale(c.coupon.getParValue()).compareTo(c.applicableAmount) > 0)
                .collect(Collectors.toList());
        List<Candidate> lessEqual = available.stream()
                .filter(c -> MoneyUtils.scale(c.coupon.getParValue()).compareTo(c.applicableAmount) <= 0)
                .collect(Collectors.toList());

        List<CouponView> finalAvailableViews = new ArrayList<>();

        if (lessEqual.isEmpty()) {
            // 全部券都“面值>可用金额”：都标可用但不默认勾选（防溢出）
            for (Candidate c : greater) {
                markUsableUnchecked(c.view, "可用但默认不勾选（面值>适用金额，防溢出）");
                finalAvailableViews.add(c.view);
            }
            result.setDiscountAmount(MoneyUtils.zero());
            result.setAvailableCouponList(finalAvailableViews);
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        // 若第一张券不可叠加，则只选这一张（对齐 AEON 的 early-return）
        if (lessEqual.get(0).coupon.getOtherAddition() == 0) {
            Candidate chosen = lessEqual.get(0);
            chosen.view.setChecked(true);
            chosen.view.setUsableStatus(CouponView.STATUS_USABLE);
            chosen.view.setUsableStatusDesc("不可叠加券优先，默认只选这一张");
            finalAvailableViews.add(chosen.view);

            for (int i = 1; i < lessEqual.size(); i++) {
                Candidate c = lessEqual.get(i);
                markUnusable(c.view, "已选券不可叠加，当前券不可用");
                finalAvailableViews.add(c.view);
            }
            for (Candidate c : greater) {
                markUnusable(c.view, "已选券不可叠加，当前券不可用");
                finalAvailableViews.add(c.view);
            }

            result.setDiscountAmount(MoneyUtils.scale(chosen.coupon.getParValue()));
            result.setAvailableCouponList(finalAvailableViews);
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        // 贪心选择：逐张尝试加入，校验互斥/叠加/溢出/模板种类上限
        Map<String, BigDecimal> remainingByCartItemId = new HashMap<>();
        for (ItemAmount a : itemAmountMap.values()) {
            remainingByCartItemId.put(a.cartItemId, a.amount);
        }

        Set<Integer> selectedTemplateIds = new LinkedHashSet<>();
        Map<Integer, Integer> selectedCountByTemplate = new HashMap<>();
        Set<Integer> mutuallyExclusiveTemplateIds = new HashSet<>();

        BigDecimal totalDiscount = MoneyUtils.zero();
        boolean anyChecked = false;

        for (Candidate c : lessEqual) {
            int templateId = c.coupon.getCouponTemplateId();

            // 模板种类上限
            if (selectedTemplateIds.size() >= goodsTemplateUseLimitCount && !selectedTemplateIds.contains(templateId)) {
                markUnusable(c.view, "模板种类达到上限(" + goodsTemplateUseLimitCount + ")");
                finalAvailableViews.add(c.view);
                continue;
            }

            // 不可与其他券叠加
            if (c.coupon.getOtherAddition() == 0) {
                markUnusable(c.view, "与其他券不可叠加");
                finalAvailableViews.add(c.view);
                continue;
            }

            // 同模板叠加上限
            int usedCount = selectedCountByTemplate.getOrDefault(templateId, 0);
            if (usedCount >= c.coupon.getSameTemplateUseLimit()) {
                markUnusable(c.view, "同模板叠加已达上限(" + c.coupon.getSameTemplateUseLimit() + ")");
                finalAvailableViews.add(c.view);
                continue;
            }

            // 互斥组
            if (mutuallyExclusiveTemplateIds.contains(templateId)) {
                markUnusable(c.view, "与已选券互斥");
                finalAvailableViews.add(c.view);
                continue;
            }

            // 金额溢出：尝试把 parValue 分摊到“可用商品剩余金额池”
            BigDecimal parValue = MoneyUtils.scale(c.coupon.getParValue());
            if (!canAllocate(parValue, c.view.getApplicableCartItemIds(), remainingByCartItemId)) {
                markUnusable(c.view, "金额溢出（适用商品剩余金额不足）");
                finalAvailableViews.add(c.view);
                continue;
            }

            allocate(parValue, c.view.getApplicableCartItemIds(), remainingByCartItemId);
            anyChecked = true;
            totalDiscount = totalDiscount.add(parValue);

            c.view.setChecked(true);
            c.view.setUsableStatus(CouponView.STATUS_USABLE);
            c.view.setUsableStatusDesc("推荐选中");
            finalAvailableViews.add(c.view);

            selectedTemplateIds.add(templateId);
            selectedCountByTemplate.put(templateId, usedCount + 1);
            mutuallyExclusiveTemplateIds.addAll(policy.getExclusiveTemplateIds(templateId));
        }

        // greater：若已选中任何券，则这些“面值>适用金额”的券默认不可用；否则可用但不选
        for (Candidate c : greater) {
            if (anyChecked) {
                markUnusable(c.view, "面值>适用金额，且已有选中券（默认不可用）");
            } else {
                markUsableUnchecked(c.view, "可用但默认不勾选（面值>适用金额，防溢出）");
            }
            finalAvailableViews.add(c.view);
        }

        result.setDiscountAmount(MoneyUtils.scale(totalDiscount));
        result.setAvailableCouponList(finalAvailableViews);
        result.setNotAvailableCouponList(notAvailable);
        return result;
    }

    public CouponCalcResult calcShippingCoupons(BigDecimal logisticFee,
                                                List<Coupon> allCoupons,
                                                Set<Integer> selectedGoodsTemplateIds,
                                                MutuallyExclusivePolicy exclusivePolicy,
                                                LocalDateTime now) {
        BigDecimal logistic = MoneyUtils.scale(logisticFee);
        List<Coupon> coupons = allCoupons == null ? Collections.emptyList() : allCoupons;
        Set<Integer> goodsTemplates = selectedGoodsTemplateIds == null ? Collections.emptySet() : selectedGoodsTemplateIds;
        MutuallyExclusivePolicy policy = exclusivePolicy == null ? new MutuallyExclusivePolicy(Collections.emptyMap()) : exclusivePolicy;
        LocalDateTime clock = now == null ? LocalDateTime.now() : now;

        CouponCalcResult result = new CouponCalcResult();

        List<Candidate> available = new ArrayList<>();
        List<CouponView> notAvailable = new ArrayList<>();

        for (Coupon c : coupons) {
            if (c.getCategory() != CouponCategory.SHIPPING) {
                continue;
            }
            Candidate cand = buildShippingCandidate(c, logistic, goodsTemplates, policy, clock);
            if (!cand.eligible) {
                notAvailable.add(cand.view);
            } else {
                available.add(cand);
            }
        }

        // 运费<=0：直接返回（所有运费券都不可用）
        if (logistic.compareTo(BigDecimal.ZERO) <= 0) {
            List<CouponView> views = new ArrayList<>();
            for (Candidate c : available) {
                markUnusable(c.view, "运费为0，运费券不可用");
                views.add(c.view);
            }
            result.setDiscountAmount(MoneyUtils.zero());
            result.setAvailableCouponList(views);
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        // 只有一张券可用：直接勾选（允许溢出）
        if (available.size() == 1) {
            Candidate only = available.get(0);
            if (Boolean.TRUE.equals(only.view.getFreightChecked())) {
                only.view.setChecked(true);
                only.view.setUsableStatus(CouponView.STATUS_USABLE);
                only.view.setUsableStatusDesc("仅一张可用券，默认选中（允许溢出）");
                result.setDiscountAmount(MoneyUtils.scale(only.coupon.getParValue()));
            } else {
                markUnusable(only.view, "与已选商品券互斥");
                result.setDiscountAmount(MoneyUtils.zero());
            }
            result.setAvailableCouponList(Collections.singletonList(only.view));
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        // 排序（对齐 AEON 运费券排序：面值高 -> 范围窄 -> 先过期 -> 门槛高）
        available.sort(Comparator
                .comparing((Candidate c) -> MoneyUtils.scale(c.coupon.getParValue())).reversed()
                .thenComparing((Candidate c) -> c.view.getConditionType(), Comparator.reverseOrder())
                .thenComparing(c -> c.coupon.getUseEndTime() == null ? LocalDateTime.MAX : c.coupon.getUseEndTime())
                .thenComparing((Candidate c) -> MoneyUtils.scale(c.coupon.getBound()), Comparator.reverseOrder()));

        List<Candidate> greater = available.stream()
                .filter(c -> MoneyUtils.scale(c.coupon.getParValue()).compareTo(logistic) > 0)
                .collect(Collectors.toList());
        List<Candidate> lessEqual = available.stream()
                .filter(c -> MoneyUtils.scale(c.coupon.getParValue()).compareTo(logistic) <= 0)
                .collect(Collectors.toList());

        List<CouponView> finalAvailableViews = new ArrayList<>();

        if (lessEqual.isEmpty()) {
            // 全部都比运费大：可用但不选（防溢出），同时要考虑 freightChecked
            for (Candidate c : greater) {
                if (!Boolean.TRUE.equals(c.view.getFreightChecked())) {
                    markUnusable(c.view, "与已选商品券互斥");
                } else {
                    markUsableUnchecked(c.view, "可用但默认不勾选（面值>运费，防溢出）");
                }
                finalAvailableViews.add(c.view);
            }
            result.setDiscountAmount(MoneyUtils.zero());
            result.setAvailableCouponList(finalAvailableViews);
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        if (lessEqual.get(0).coupon.getOtherAddition() == 0) {
            Candidate chosen = lessEqual.get(0);
            if (!Boolean.TRUE.equals(chosen.view.getFreightChecked())) {
                markUnusable(chosen.view, "与已选商品券互斥");
                finalAvailableViews.add(chosen.view);
                for (int i = 1; i < lessEqual.size(); i++) {
                    Candidate c = lessEqual.get(i);
                    markUnusable(c.view, "与已选券不可叠加/或互斥");
                    finalAvailableViews.add(c.view);
                }
                for (Candidate c : greater) {
                    markUnusable(c.view, "与已选券不可叠加/或互斥");
                    finalAvailableViews.add(c.view);
                }
                result.setDiscountAmount(MoneyUtils.zero());
                result.setAvailableCouponList(finalAvailableViews);
                result.setNotAvailableCouponList(notAvailable);
                return result;
            }

            chosen.view.setChecked(true);
            chosen.view.setUsableStatus(CouponView.STATUS_USABLE);
            chosen.view.setUsableStatusDesc("不可叠加券优先，默认只选这一张");
            finalAvailableViews.add(chosen.view);

            for (int i = 1; i < lessEqual.size(); i++) {
                Candidate c = lessEqual.get(i);
                markUnusable(c.view, "已选券不可叠加，当前券不可用");
                finalAvailableViews.add(c.view);
            }
            for (Candidate c : greater) {
                markUnusable(c.view, "已选券不可叠加，当前券不可用");
                finalAvailableViews.add(c.view);
            }

            result.setDiscountAmount(MoneyUtils.scale(chosen.coupon.getParValue()));
            result.setAvailableCouponList(finalAvailableViews);
            result.setNotAvailableCouponList(notAvailable);
            return result;
        }

        BigDecimal remaining = logistic;
        Set<Integer> selectedTemplates = new LinkedHashSet<>();
        Map<Integer, Integer> selectedCountByTemplate = new HashMap<>();
        Set<Integer> mutuallyExclusiveTemplateIds = new HashSet<>();
        BigDecimal totalDiscount = MoneyUtils.zero();

        for (Candidate c : lessEqual) {
            int templateId = c.coupon.getCouponTemplateId();

            if (!Boolean.TRUE.equals(c.view.getFreightChecked())) {
                markUnusable(c.view, "与已选商品券互斥");
                finalAvailableViews.add(c.view);
                continue;
            }

            if (mutuallyExclusiveTemplateIds.contains(templateId)) {
                markUnusable(c.view, "与已选运费券互斥");
                finalAvailableViews.add(c.view);
                continue;
            }

            // 与其他券不可叠加：只要已选过其他模板就不让选
            if (c.coupon.getOtherAddition() == 0 && !selectedTemplates.isEmpty() && !selectedTemplates.contains(templateId)) {
                markUnusable(c.view, "与其他券不可叠加");
                finalAvailableViews.add(c.view);
                continue;
            }

            int usedCount = selectedCountByTemplate.getOrDefault(templateId, 0);
            if (usedCount >= c.coupon.getSameTemplateUseLimit()) {
                markUnusable(c.view, "同模板叠加已达上限(" + c.coupon.getSameTemplateUseLimit() + ")");
                finalAvailableViews.add(c.view);
                continue;
            }

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                markUnusable(c.view, "运费金额已被抵扣完");
                finalAvailableViews.add(c.view);
                continue;
            }

            BigDecimal parValue = MoneyUtils.scale(c.coupon.getParValue());
            if (remaining.subtract(parValue).compareTo(BigDecimal.ZERO) < 0) {
                markUsableUnchecked(c.view, "可用但默认不勾选（抵扣后将溢出，防溢出）");
                finalAvailableViews.add(c.view);
                continue;
            }

            // 选中
            c.view.setChecked(true);
            c.view.setUsableStatus(CouponView.STATUS_USABLE);
            c.view.setUsableStatusDesc("推荐选中");
            finalAvailableViews.add(c.view);

            remaining = remaining.subtract(parValue);
            totalDiscount = totalDiscount.add(parValue);
            selectedTemplates.add(templateId);
            selectedCountByTemplate.put(templateId, usedCount + 1);
            mutuallyExclusiveTemplateIds.addAll(policy.getExclusiveTemplateIds(templateId));
        }

        for (Candidate c : greater) {
            if (!Boolean.TRUE.equals(c.view.getFreightChecked())) {
                markUnusable(c.view, "与已选商品券互斥");
            } else if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                markUnusable(c.view, "运费金额已被抵扣完");
            } else {
                markUsableUnchecked(c.view, "可用但默认不勾选（面值>运费，防溢出）");
            }
            finalAvailableViews.add(c.view);
        }

        result.setDiscountAmount(MoneyUtils.scale(totalDiscount));
        result.setAvailableCouponList(finalAvailableViews);
        result.setNotAvailableCouponList(notAvailable);
        return result;
    }

    private static Candidate buildGoodsCandidate(Coupon coupon, Map<String, ItemAmount> itemAmountMap, LocalDateTime now) {
        Candidate cand = new Candidate();
        cand.coupon = coupon;
        cand.view = buildBaseView(coupon);
        cand.view.setFreightChecked(null);

        List<String> applicableItemIds = new ArrayList<>();
        BigDecimal applicableAmount = MoneyUtils.zero();

        for (ItemAmount a : itemAmountMap.values()) {
            if (coupon.getConditionType() == CouponConditionType.ALL || coupon.matchesSku(a.skuId)) {
                applicableItemIds.add(a.cartItemId);
                applicableAmount = applicableAmount.add(a.amount);
            }
        }
        applicableAmount = MoneyUtils.scale(applicableAmount);

        cand.view.setApplicableCartItemIds(applicableItemIds);
        cand.view.setApplicableAmount(applicableAmount);
        cand.applicableAmount = applicableAmount;

        // 资格校验：时间/门槛/适用金额
        if (coupon.getUseEndTime() != null && now.isAfter(coupon.getUseEndTime())) {
            markUnusable(cand.view, "已过期");
            cand.eligible = false;
            return cand;
        }
        BigDecimal bound = MoneyUtils.scale(coupon.getBound());
        if (applicableAmount.compareTo(bound) < 0) {
            markUnusable(cand.view, "未达门槛(bound=" + bound + ")");
            cand.eligible = false;
            return cand;
        }
        if (applicableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            markUnusable(cand.view, "适用金额为0");
            cand.eligible = false;
            return cand;
        }

        markUsableUnchecked(cand.view, "可用");
        cand.eligible = true;
        return cand;
    }

    private static Candidate buildShippingCandidate(Coupon coupon,
                                                   BigDecimal logisticFee,
                                                   Set<Integer> selectedGoodsTemplateIds,
                                                   MutuallyExclusivePolicy policy,
                                                   LocalDateTime now) {
        Candidate cand = new Candidate();
        cand.coupon = coupon;
        cand.view = buildBaseView(coupon);

        cand.view.setApplicableAmount(logisticFee);
        cand.applicableAmount = logisticFee;

        boolean freightChecked = !policy.isExclusiveWithAny(coupon.getCouponTemplateId(), selectedGoodsTemplateIds);
        cand.view.setFreightChecked(freightChecked);

        if (coupon.getUseEndTime() != null && now.isAfter(coupon.getUseEndTime())) {
            markUnusable(cand.view, "已过期");
            cand.eligible = false;
            return cand;
        }

        BigDecimal bound = MoneyUtils.scale(coupon.getBound());
        if (logisticFee.compareTo(bound) < 0) {
            markUnusable(cand.view, "未达门槛(bound=" + bound + ")");
            cand.eligible = false;
            return cand;
        }

        // freightChecked=false：这里不放进 notAvailable，而是放进 available 但不可用（更接近 AEON 的展示）
        if (!freightChecked) {
            markUnusable(cand.view, "与已选商品券互斥");
        } else {
            markUsableUnchecked(cand.view, "可用");
        }
        cand.eligible = true;
        return cand;
    }

    private static CouponView buildBaseView(Coupon c) {
        CouponView v = new CouponView();
        v.setCouponNo(c.getCouponNo());
        v.setCouponTemplateId(c.getCouponTemplateId());
        v.setCategory(c.getCategory());
        v.setParValue(MoneyUtils.scale(c.getParValue()));
        v.setBound(MoneyUtils.scale(c.getBound()));
        v.setOtherAddition(c.getOtherAddition());
        v.setConditionType(c.getConditionType() == null ? 0 : c.getConditionType().getCode());
        v.setChecked(false);
        v.setUsableStatus(CouponView.STATUS_USABLE);
        v.setUsableStatusDesc("可用");
        return v;
    }

    private static Map<String, ItemAmount> buildItemAmountMap(List<PromoItemResult> promoItems) {
        Map<String, ItemAmount> map = new LinkedHashMap<>();
        for (PromoItemResult item : promoItems) {
            ItemAmount a = new ItemAmount();
            a.cartItemId = item.getCartItemId();
            a.skuId = item.getSkuId();
            // 对齐 AEON 券入参：promoPrice 字段本质是“商品净额(行总价)”
            // 这里直接使用促销引擎返回的 promoAmount，避免“单价四舍五入再乘数量”带来的 0.01 误差。
            a.amount = MoneyUtils.scale(item.getPromoAmount());
            map.put(a.cartItemId, a);
        }
        return map;
    }

    private static boolean canAllocate(BigDecimal discount,
                                       List<String> cartItemIds,
                                       Map<String, BigDecimal> remainingByCartItemId) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (String id : cartItemIds) {
            BigDecimal rem = remainingByCartItemId.get(id);
            if (rem != null && rem.compareTo(BigDecimal.ZERO) > 0) {
                sum = sum.add(rem);
            }
        }
        sum = MoneyUtils.scale(sum);
        return sum.compareTo(discount) >= 0;
    }

    private static void allocate(BigDecimal discount,
                                 List<String> cartItemIds,
                                 Map<String, BigDecimal> remainingByCartItemId) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        // 以“剩余可用金额”为权重分摊
        Map<String, BigDecimal> weights = new LinkedHashMap<>();
        for (String id : cartItemIds) {
            BigDecimal rem = remainingByCartItemId.get(id);
            if (rem != null && rem.compareTo(BigDecimal.ZERO) > 0) {
                weights.put(id, rem);
            }
        }
        Map<String, BigDecimal> allocated = MoneyAllocator.allocate(discount, weights);
        for (Map.Entry<String, BigDecimal> e : allocated.entrySet()) {
            String id = e.getKey();
            BigDecimal alloc = MoneyUtils.scale(e.getValue());
            BigDecimal rem = remainingByCartItemId.getOrDefault(id, MoneyUtils.zero());
            remainingByCartItemId.put(id, MoneyUtils.scale(rem.subtract(alloc)));
        }
    }

    private static void markUsableUnchecked(CouponView view, String desc) {
        view.setChecked(false);
        view.setUsableStatus(CouponView.STATUS_USABLE);
        view.setUsableStatusDesc(desc);
    }

    private static void markUnusable(CouponView view, String desc) {
        view.setChecked(false);
        view.setUsableStatus(CouponView.STATUS_UN_USABLE);
        view.setUsableStatusDesc(desc);
    }

    private static class ItemAmount {
        String cartItemId;
        String skuId;
        BigDecimal amount;
    }

    private static class Candidate {
        Coupon coupon;
        CouponView view;
        boolean eligible;
        BigDecimal applicableAmount;
    }
}
