package com.bruce.coupondemo.engine;

import com.bruce.coupondemo.model.Coupon;
import com.bruce.coupondemo.model.CouponResult;
import com.bruce.coupondemo.model.CouponType;
import com.bruce.promotiondemo.model.CartItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 贪心选券引擎
 * <p>
 * 参考永旺 StoreCalcCouponLogic 的核心逻辑：
 * 1. 过滤：过期、门槛、SKU范围
 * 2. 分组：商品券 vs 运费券独立计算
 * 3. 排序：面值高→范围窄→先过期→门槛高
 * 4. 贪心逐张选择，检查互斥组和溢出
 */
public class CouponEngine {

    /** 默认最大可选券数 */
    private static final int DEFAULT_MAX_COUPONS = 3;

    private final int maxCoupons;

    public CouponEngine() {
        this(DEFAULT_MAX_COUPONS);
    }

    public CouponEngine(int maxCoupons) {
        this.maxCoupons = maxCoupons;
    }

    /**
     * 计算券优惠
     *
     * @param priceAfterPromotion 促销后的商品总价（券基于此价格判断门槛和抵扣）
     * @param shippingFee         运费
     * @param coupons             用户持有的券列表
     * @param cartItems           购物车商品（用于SKU范围校验）
     * @return 券计算结果
     */
    public CouponResult calculate(BigDecimal priceAfterPromotion, BigDecimal shippingFee,
                                  List<Coupon> coupons, List<CartItem> cartItems) {
        if (coupons == null || coupons.isEmpty()) {
            return CouponResult.builder().build();
        }

        // 收集购物车中的SKU
        Set<String> cartSkuCodes = cartItems.stream()
                .map(CartItem::getSkuCode)
                .collect(Collectors.toSet());

        // 1. 过滤可用券
        List<Coupon> availableCoupons = coupons.stream()
                .map(Coupon::copy)
                .filter(c -> isAvailable(c, priceAfterPromotion, shippingFee, cartSkuCodes))
                .collect(Collectors.toList());

        // 2. 分组：商品券 vs 运费券
        List<Coupon> productCoupons = availableCoupons.stream()
                .filter(c -> c.getCouponType() == CouponType.PRODUCT)
                .collect(Collectors.toList());
        List<Coupon> shippingCoupons = availableCoupons.stream()
                .filter(c -> c.getCouponType() == CouponType.SHIPPING)
                .collect(Collectors.toList());

        // 3. 各自贪心选券
        BigDecimal productDiscount = greedySelect(productCoupons, priceAfterPromotion);
        BigDecimal shippingDiscount = greedySelect(shippingCoupons, shippingFee);

        // 4. 合并结果
        List<Coupon> allAvailable = new ArrayList<>();
        allAvailable.addAll(productCoupons);
        allAvailable.addAll(shippingCoupons);

        return CouponResult.builder()
                .availableCoupons(allAvailable)
                .productCouponDiscount(productDiscount)
                .shippingCouponDiscount(shippingDiscount)
                .build();
    }

    /**
     * 检查券是否可用
     */
    private boolean isAvailable(Coupon coupon, BigDecimal productPrice, BigDecimal shippingFee,
                                Set<String> cartSkuCodes) {
        // 过期检查
        if (coupon.getExpireDate() != null && coupon.getExpireDate().isBefore(LocalDate.now())) {
            return false;
        }

        // 门槛检查（基于促销后价格）
        BigDecimal checkPrice = coupon.getCouponType() == CouponType.SHIPPING ? shippingFee : productPrice;
        if (coupon.getThreshold() != null && coupon.getThreshold().compareTo(BigDecimal.ZERO) > 0) {
            if (checkPrice.compareTo(coupon.getThreshold()) < 0) {
                return false;
            }
        }

        // SKU范围检查（仅商品券需要）
        if (coupon.getCouponType() == CouponType.PRODUCT
                && coupon.getApplicableSkuCodes() != null
                && !coupon.getApplicableSkuCodes().isEmpty()) {
            boolean hasMatch = coupon.getApplicableSkuCodes().stream()
                    .anyMatch(cartSkuCodes::contains);
            if (!hasMatch) {
                return false;
            }
        }

        return true;
    }

    /**
     * 贪心选券
     *
     * @param coupons       已过滤的同类型券列表（会被修改checked和actualDiscount）
     * @param remainAmount  可抵扣的剩余金额
     * @return 总优惠金额
     */
    private BigDecimal greedySelect(List<Coupon> coupons, BigDecimal remainAmount) {
        if (coupons.isEmpty() || remainAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 排序：面值高→范围窄→先过期→门槛高
        coupons.sort(Comparator
                .comparing(Coupon::getFaceValue, Comparator.reverseOrder())
                .thenComparing(c -> c.getApplicableSkuCodes() == null ? Integer.MAX_VALUE : c.getApplicableSkuCodes().size())
                .thenComparing(Comparator.comparing(Coupon::getExpireDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .thenComparing(c -> c.getThreshold() == null ? BigDecimal.ZERO : c.getThreshold(), Comparator.reverseOrder())
        );

        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal currentRemain = remainAmount;
        int selectedCount = 0;
        Set<String> usedExclusiveGroups = new HashSet<>();

        for (Coupon coupon : coupons) {
            // 总券数上限
            if (selectedCount >= maxCoupons) {
                break;
            }
            // 剩余金额已为0
            if (currentRemain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            // 互斥组检查
            if (coupon.getExclusiveGroupId() != null
                    && usedExclusiveGroups.contains(coupon.getExclusiveGroupId())) {
                continue;
            }

            // 溢出检测：实际优惠 = min(面值, 剩余金额)
            BigDecimal actualDiscount = coupon.getFaceValue().min(currentRemain);

            coupon.setChecked(true);
            coupon.setActualDiscount(actualDiscount);
            totalDiscount = totalDiscount.add(actualDiscount);
            currentRemain = currentRemain.subtract(actualDiscount);
            selectedCount++;

            if (coupon.getExclusiveGroupId() != null) {
                usedExclusiveGroups.add(coupon.getExclusiveGroupId());
            }
        }

        return totalDiscount;
    }
}
