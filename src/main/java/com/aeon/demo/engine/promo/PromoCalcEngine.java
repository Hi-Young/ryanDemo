package com.aeon.demo.engine.promo;

import com.aeon.demo.domain.CartItem;
import com.aeon.demo.domain.Promotion;
import com.aeon.demo.domain.PromotionLevel;
import com.aeon.demo.domain.PromotionType;
import com.aeon.demo.util.MoneyAllocator;
import com.aeon.demo.util.MoneyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 促销计算引擎（最小化）：核心只做一件事——“取低价”(ChooseLowPrice)。
 *
 * <p>实现思路对齐《永旺券&促销.md》：</p>
 * <ul>
 *   <li>每个商品命中多个促销 → 需要选择“每个商品到底用哪一个促销”</li>
 *   <li>组合层级采用笛卡尔积穷举所有分配方案 → 逐方案计算总优惠 → 取总优惠最大(价格最低)</li>
 * </ul>
 *
 * @author codex
 */
public class PromoCalcEngine {

    /**
     * 避免 Demo 被误用在大购物车导致组合爆炸；超过阈值则退化为“只算单品取低”。
     */
    private final long maxEnumerations;

    public PromoCalcEngine() {
        this(50_000L);
    }

    public PromoCalcEngine(long maxEnumerations) {
        this.maxEnumerations = maxEnumerations;
    }

    public PromoCalcResult calc(List<CartItem> cartItems, List<Promotion> promotions) {
        List<CartItem> items = cartItems == null ? Collections.emptyList() : cartItems;
        List<Promotion> promoList = promotions == null ? Collections.emptyList() : promotions;

        PromoCalcResult result = new PromoCalcResult();
        if (items.isEmpty()) {
            result.setOriginalGoodsAmount(MoneyUtils.zero());
            result.setPromoGoodsAmount(MoneyUtils.zero());
            result.setEvaluatedPlanCount(0);
            return result;
        }

        BigDecimal originalTotal = MoneyUtils.zero();
        for (CartItem item : items) {
            originalTotal = originalTotal.add(lineAmount(item.getSalePrice(), item.getQuantity()));
        }
        originalTotal = MoneyUtils.scale(originalTotal);

        // promoId -> Promotion
        Map<Long, Promotion> promoMap = new HashMap<>();
        for (Promotion p : promoList) {
            promoMap.put(p.getPromoId(), p);
        }

        // 每个商品的可选促销：NONE(0) + 命中的促销
        List<List<Long>> optionsByIndex = new ArrayList<>();
        long estimated = 1;
        for (CartItem item : items) {
            List<Long> options = new ArrayList<>();
            options.add(0L); // 不参加任何促销
            for (Promotion p : promoList) {
                if (p.matchesSku(item.getSkuId())) {
                    options.add(p.getPromoId());
                }
            }
            optionsByIndex.add(options);
            estimated = estimated * options.size();
            if (estimated > maxEnumerations) {
                break;
            }
        }

        BestPlan best;
        if (estimated > maxEnumerations) {
            // 退化：只对单品促销做“取低”，不做组合穷举（避免指数级爆炸）
            best = greedySingleOnly(items, promoList);
            result.setEvaluatedPlanCount(-1); // -1 表示退化路径
        } else {
            best = enumerateChooseLowPrice(items, promoMap, optionsByIndex);
            result.setEvaluatedPlanCount(best == null ? 0 : best.evaluatedCount);
        }

        if (best == null) {
            result.setOriginalGoodsAmount(originalTotal);
            result.setPromoGoodsAmount(originalTotal);
            return result;
        }

        result.setOriginalGoodsAmount(originalTotal);
        result.setPromoGoodsAmount(best.totalPromoAmount);
        result.setItems(best.itemResults);
        result.setAdjusts(best.adjusts);
        return result;
    }

    private BestPlan greedySingleOnly(List<CartItem> items, List<Promotion> promoList) {
        // 只在单品促销里选一个最低价（组合促销全部忽略）
        List<PromoItemResult> itemResults = new ArrayList<>();
        Map<Long, PromoAdjustAccumulator> acc = new HashMap<>();

        BigDecimal total = MoneyUtils.zero();
        for (CartItem item : items) {
            BigDecimal salePrice = MoneyUtils.scale(item.getSalePrice());
            BigDecimal bestPrice = salePrice;
            long bestPromoId = 0L;
            String bestPromoName = "NONE";

            for (Promotion p : promoList) {
                if (p.getLevel() != PromotionLevel.SINGLE) {
                    continue;
                }
                if (!p.matchesSku(item.getSkuId())) {
                    continue;
                }
                BigDecimal promoPrice = applySinglePromoUnitPrice(p, salePrice, item.getQuantity());
                if (promoPrice.compareTo(bestPrice) < 0) {
                    bestPrice = promoPrice;
                    bestPromoId = p.getPromoId();
                    bestPromoName = p.getName();
                }
            }

            BigDecimal saleAmount = lineAmount(salePrice, item.getQuantity());
            BigDecimal promoAmount = lineAmount(bestPrice, item.getQuantity());
            BigDecimal discount = MoneyUtils.scale(saleAmount.subtract(promoAmount));

            PromoItemResult r = new PromoItemResult();
            r.setCartItemId(item.getCartItemId());
            r.setSkuId(item.getSkuId());
            r.setQuantity(item.getQuantity());
            r.setSalePrice(salePrice);
            r.setPromoPrice(bestPrice);
            r.setSaleAmount(saleAmount);
            r.setPromoAmount(promoAmount);
            r.setAppliedPromoId(bestPromoId);
            r.setAppliedPromoName(bestPromoName);
            r.setPromoDiscountAmount(discount);
            itemResults.add(r);

            if (bestPromoId != 0L && discount.compareTo(BigDecimal.ZERO) > 0) {
                final long promoIdFinal = bestPromoId;
                final String promoNameFinal = bestPromoName;
                acc.computeIfAbsent(promoIdFinal, k -> new PromoAdjustAccumulator(promoIdFinal, promoNameFinal))
                        .add(discount);
            }

            total = total.add(promoAmount);
        }

        BestPlan plan = new BestPlan();
        plan.totalPromoAmount = MoneyUtils.scale(total);
        plan.itemResults = itemResults;
        plan.adjusts = toAdjusts(acc);
        plan.evaluatedCount = 0;
        return plan;
    }

    private BestPlan enumerateChooseLowPrice(List<CartItem> items,
                                            Map<Long, Promotion> promoMap,
                                            List<List<Long>> optionsByIndex) {
        BestPlan best = new BestPlan();
        best.totalPromoAmount = null; // 未初始化
        best.evaluatedCount = 0;

        List<Long> selected = new ArrayList<>(items.size());
        dfsEnumerate(0, items, promoMap, optionsByIndex, selected, best);
        return best.totalPromoAmount == null ? null : best;
    }

    private void dfsEnumerate(int idx,
                              List<CartItem> items,
                              Map<Long, Promotion> promoMap,
                              List<List<Long>> optionsByIndex,
                              List<Long> selectedPromoIds,
                              BestPlan best) {
        if (idx == items.size()) {
            best.evaluatedCount++;
            EvaluatedPlan evaluated = evaluate(items, promoMap, selectedPromoIds);
            if (evaluated == null) {
                return;
            }
            if (best.totalPromoAmount == null
                    || evaluated.totalPromoAmount.compareTo(best.totalPromoAmount) < 0
                    || (evaluated.totalPromoAmount.compareTo(best.totalPromoAmount) == 0
                    && evaluated.totalDiscountAmount.compareTo(best.totalDiscountAmount) > 0)) {
                best.totalPromoAmount = evaluated.totalPromoAmount;
                best.totalDiscountAmount = evaluated.totalDiscountAmount;
                best.itemResults = evaluated.itemResults;
                best.adjusts = evaluated.adjusts;
            }
            return;
        }

        for (Long promoId : optionsByIndex.get(idx)) {
            selectedPromoIds.add(promoId);
            dfsEnumerate(idx + 1, items, promoMap, optionsByIndex, selectedPromoIds, best);
            selectedPromoIds.remove(selectedPromoIds.size() - 1);
        }
    }

    private EvaluatedPlan evaluate(List<CartItem> items,
                                  Map<Long, Promotion> promoMap,
                                  List<Long> selectedPromoIds) {
        // itemIndex -> state
        List<ItemState> states = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            BigDecimal salePrice = MoneyUtils.scale(item.getSalePrice());
            ItemState s = new ItemState();
            s.cartItemId = item.getCartItemId();
            s.skuId = item.getSkuId();
            s.quantity = item.getQuantity();
            s.salePrice = salePrice;
            s.saleAmount = lineAmount(salePrice, item.getQuantity());
            s.promoPrice = salePrice;
            s.promoAmount = s.saleAmount;
            s.appliedPromoId = 0L;
            s.appliedPromoName = "NONE";
            states.add(s);
        }

        // promoId -> itemIndex list
        Map<Long, List<Integer>> assigned = new HashMap<>();
        for (int i = 0; i < selectedPromoIds.size(); i++) {
            Long promoId = selectedPromoIds.get(i);
            assigned.computeIfAbsent(promoId, k -> new ArrayList<>()).add(i);
        }

        // 先处理单品，再处理组合（本 Demo 简化：两者互斥；顺序不影响，但便于阅读）
        for (Map.Entry<Long, List<Integer>> e : assigned.entrySet()) {
            long promoId = e.getKey();
            if (promoId == 0L) {
                continue;
            }
            Promotion p = promoMap.get(promoId);
            if (p == null || p.getLevel() != PromotionLevel.SINGLE) {
                continue;
            }
            for (Integer idx : e.getValue()) {
                ItemState s = states.get(idx);
                BigDecimal newPromoPrice = applySinglePromoUnitPrice(p, s.salePrice, s.quantity);
                BigDecimal promoAmount = lineAmount(newPromoPrice, s.quantity);
                BigDecimal discount = MoneyUtils.scale(s.saleAmount.subtract(promoAmount));
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    s.promoPrice = newPromoPrice;
                    s.promoAmount = promoAmount;
                    s.appliedPromoId = p.getPromoId();
                    s.appliedPromoName = p.getName();
                }
            }
        }

        for (Map.Entry<Long, List<Integer>> e : assigned.entrySet()) {
            long promoId = e.getKey();
            if (promoId == 0L) {
                continue;
            }
            Promotion p = promoMap.get(promoId);
            if (p == null || p.getLevel() != PromotionLevel.GROUP) {
                continue;
            }
            applyGroupPromotion(p, states, e.getValue());
        }

        BigDecimal totalPromo = MoneyUtils.zero();
        BigDecimal totalDiscount = MoneyUtils.zero();
        Map<Long, PromoAdjustAccumulator> acc = new HashMap<>();

        List<PromoItemResult> itemResults = new ArrayList<>();
        for (ItemState s : states) {
            BigDecimal promoAmount = MoneyUtils.scale(s.promoAmount);
            BigDecimal discount = MoneyUtils.scale(s.saleAmount.subtract(promoAmount));
            totalPromo = totalPromo.add(promoAmount);
            totalDiscount = totalDiscount.add(discount);

            PromoItemResult r = new PromoItemResult();
            r.setCartItemId(s.cartItemId);
            r.setSkuId(s.skuId);
            r.setQuantity(s.quantity);
            r.setSalePrice(s.salePrice);
            r.setPromoPrice(s.promoPrice);
            r.setSaleAmount(s.saleAmount);
            r.setPromoAmount(promoAmount);
            r.setAppliedPromoId(s.appliedPromoId);
            r.setAppliedPromoName(s.appliedPromoName);
            r.setPromoDiscountAmount(discount);
            itemResults.add(r);

            if (s.appliedPromoId != 0L && discount.compareTo(BigDecimal.ZERO) > 0) {
                acc.computeIfAbsent(s.appliedPromoId, k -> new PromoAdjustAccumulator(s.appliedPromoId, s.appliedPromoName))
                        .add(discount);
            }
        }

        EvaluatedPlan plan = new EvaluatedPlan();
        plan.totalPromoAmount = MoneyUtils.scale(totalPromo);
        plan.totalDiscountAmount = MoneyUtils.scale(totalDiscount);
        plan.itemResults = itemResults;
        plan.adjusts = toAdjusts(acc);
        return plan;
    }

    private void applyGroupPromotion(Promotion promo, List<ItemState> states, List<Integer> itemIndexes) {
        if (itemIndexes == null || itemIndexes.isEmpty()) {
            return;
        }

        BigDecimal groupTotal = MoneyUtils.zero();
        Map<Integer, BigDecimal> weights = new LinkedHashMap<>();
        for (Integer idx : itemIndexes) {
            ItemState s = states.get(idx);
            // 组合促销通常基于“参与商品的当前金额”，本 Demo 简化为“原价金额”
            groupTotal = groupTotal.add(s.saleAmount);
            weights.put(idx, s.saleAmount);
        }
        groupTotal = MoneyUtils.scale(groupTotal);

        BigDecimal discountTotal = calcGroupDiscount(promo, groupTotal);
        if (discountTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 防止极端配置导致折扣超过金额
        discountTotal = MoneyUtils.min(discountTotal, groupTotal);

        Map<Integer, BigDecimal> allocated = MoneyAllocator.allocate(discountTotal, weights);
        for (Map.Entry<Integer, BigDecimal> e : allocated.entrySet()) {
            Integer idx = e.getKey();
            BigDecimal discount = MoneyUtils.scale(e.getValue());

            ItemState s = states.get(idx);
            BigDecimal promoAmount = MoneyUtils.scale(s.saleAmount.subtract(discount));
            BigDecimal unitPromoPrice = promoAmount.divide(new BigDecimal(s.quantity), MoneyUtils.MONEY_SCALE, RoundingMode.HALF_UP);

            // 组合促销若更优，则覆盖单品结果（模拟“取低价”时的最终分配方案）
            BigDecimal currentPromoAmount = MoneyUtils.scale(s.promoAmount);
            if (promoAmount.compareTo(currentPromoAmount) < 0) {
                s.promoPrice = unitPromoPrice;
                s.promoAmount = promoAmount;
                s.appliedPromoId = promo.getPromoId();
                s.appliedPromoName = promo.getName();
            }
        }
    }

    private BigDecimal calcGroupDiscount(Promotion promo, BigDecimal groupTotal) {
        if (promo.getType() == PromotionType.FULL_REDUCTION) {
            BigDecimal threshold = MoneyUtils.scale(promo.getThreshold());
            BigDecimal reduce = MoneyUtils.scale(promo.getReduceAmount());
            if (groupTotal.compareTo(threshold) >= 0) {
                return reduce;
            }
            return MoneyUtils.zero();
        }
        if (promo.getType() == PromotionType.DISCOUNT_RATE) {
            BigDecimal rate = promo.getDiscountRate();
            if (rate == null) {
                return MoneyUtils.zero();
            }
            BigDecimal promoTotal = groupTotal.multiply(rate).setScale(MoneyUtils.MONEY_SCALE, RoundingMode.HALF_UP);
            return MoneyUtils.scale(groupTotal.subtract(promoTotal));
        }
        // DIRECT_REDUCTION 对组合层级意义不大，此处不实现
        return MoneyUtils.zero();
    }

    private BigDecimal applySinglePromoUnitPrice(Promotion promo, BigDecimal salePrice, int quantity) {
        if (promo.getType() == PromotionType.DIRECT_REDUCTION) {
            BigDecimal reduce = MoneyUtils.scale(promo.getReduceAmount());
            BigDecimal newPrice = salePrice.subtract(reduce);
            if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
                newPrice = BigDecimal.ZERO;
            }
            return MoneyUtils.scale(newPrice);
        }
        if (promo.getType() == PromotionType.DISCOUNT_RATE) {
            BigDecimal rate = promo.getDiscountRate();
            if (rate == null) {
                return MoneyUtils.scale(salePrice);
            }
            return MoneyUtils.scale(salePrice.multiply(rate));
        }
        if (promo.getType() == PromotionType.FULL_REDUCTION) {
            BigDecimal threshold = MoneyUtils.scale(promo.getThreshold());
            BigDecimal reduce = MoneyUtils.scale(promo.getReduceAmount());
            BigDecimal saleAmount = lineAmount(salePrice, quantity);
            if (saleAmount.compareTo(threshold) >= 0) {
                BigDecimal promoAmount = saleAmount.subtract(MoneyUtils.min(reduce, saleAmount));
                return promoAmount.divide(new BigDecimal(quantity), MoneyUtils.MONEY_SCALE, RoundingMode.HALF_UP);
            }
            return MoneyUtils.scale(salePrice);
        }
        return MoneyUtils.scale(salePrice);
    }

    private static BigDecimal lineAmount(BigDecimal unitPrice, int quantity) {
        return MoneyUtils.scale(unitPrice).multiply(new BigDecimal(quantity)).setScale(MoneyUtils.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static List<PromoAdjustResult> toAdjusts(Map<Long, PromoAdjustAccumulator> acc) {
        List<PromoAdjustResult> adjusts = new ArrayList<>();
        for (PromoAdjustAccumulator a : acc.values()) {
            adjusts.add(new PromoAdjustResult(a.promoId, a.promoName, MoneyUtils.scale(a.amount)));
        }
        // 为了阅读，按优惠金额降序
        adjusts.sort((o1, o2) -> o2.getPromoAmount().compareTo(o1.getPromoAmount()));
        return adjusts;
    }

    private static class ItemState {
        String cartItemId;
        String skuId;
        int quantity;

        BigDecimal salePrice;
        BigDecimal saleAmount;

        BigDecimal promoPrice;
        BigDecimal promoAmount;
        long appliedPromoId;
        String appliedPromoName;
    }

    private static class PromoAdjustAccumulator {
        final long promoId;
        final String promoName;
        BigDecimal amount = BigDecimal.ZERO;

        PromoAdjustAccumulator(long promoId, String promoName) {
            this.promoId = promoId;
            this.promoName = promoName;
        }

        void add(BigDecimal delta) {
            if (delta == null) {
                return;
            }
            amount = amount.add(delta);
        }
    }

    private static class EvaluatedPlan {
        BigDecimal totalPromoAmount;
        BigDecimal totalDiscountAmount;
        List<PromoItemResult> itemResults;
        List<PromoAdjustResult> adjusts;
    }

    private static class BestPlan {
        BigDecimal totalPromoAmount;
        BigDecimal totalDiscountAmount = MoneyUtils.zero();
        List<PromoItemResult> itemResults = new ArrayList<>();
        List<PromoAdjustResult> adjusts = new ArrayList<>();
        long evaluatedCount;
    }
}
