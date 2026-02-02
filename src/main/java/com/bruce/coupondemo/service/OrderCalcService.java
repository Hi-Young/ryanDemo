package com.bruce.coupondemo.service;

import com.bruce.coupondemo.engine.CouponEngine;
import com.bruce.coupondemo.model.Coupon;
import com.bruce.coupondemo.model.CouponResult;
import com.bruce.coupondemo.model.OrchestrationResult;
import com.bruce.promotiondemo.engine.NewPromotionEngine;
import com.bruce.promotiondemo.model.Cart;
import com.bruce.promotiondemo.model.CartItem;
import com.bruce.promotiondemo.model.Rule;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 订单计算编排服务
 * <p>
 * 核心流程：先促销 → 后用券 → 汇总结果
 * 引用已有的 NewPromotionEngine 作为促销引擎
 */
public class OrderCalcService {

    private final NewPromotionEngine promotionEngine = new NewPromotionEngine();
    private final CouponEngine couponEngine = new CouponEngine();

    /**
     * 计算订单：促销 + 券联动
     *
     * @param items       购物车商品
     * @param rules       促销规则（可为空）
     * @param coupons     用户持有的券（可为空）
     * @param shippingFee 运费
     * @return 编排结果
     */
    public OrchestrationResult calculate(List<CartItem> items, List<Rule> rules,
                                         List<Coupon> coupons, BigDecimal shippingFee) {
        if (shippingFee == null) {
            shippingFee = BigDecimal.ZERO;
        }
        if (rules == null) {
            rules = Collections.emptyList();
        }
        if (coupons == null) {
            coupons = Collections.emptyList();
        }

        // ====== Step 1: 促销计算 ======
        Cart cart = Cart.builder().items(items).build();
        BigDecimal originalPrice = cart.getTotalPrice();

        Cart promotedCart;
        BigDecimal promotionDiscount;
        BigDecimal priceAfterPromotion;

        if (!rules.isEmpty()) {
            promotedCart = promotionEngine.calculate(cart, rules);
            promotionDiscount = promotedCart.getTotalDiscount();
            priceAfterPromotion = promotedCart.getPayPrice();
        } else {
            promotedCart = cart;
            promotionDiscount = BigDecimal.ZERO;
            priceAfterPromotion = originalPrice;
        }

        // ====== Step 2: 券计算（基于促销后价格） ======
        CouponResult couponResult = couponEngine.calculate(
                priceAfterPromotion, shippingFee, coupons, items);

        // ====== Step 3: 汇总结果 ======
        BigDecimal productCouponDiscount = couponResult.getProductCouponDiscount();
        BigDecimal shippingCouponDiscount = couponResult.getShippingCouponDiscount();

        BigDecimal productPayPrice = priceAfterPromotion.subtract(productCouponDiscount)
                .max(BigDecimal.ZERO);
        BigDecimal shippingPayPrice = shippingFee.subtract(shippingCouponDiscount)
                .max(BigDecimal.ZERO);
        BigDecimal totalPayPrice = productPayPrice.add(shippingPayPrice);

        return OrchestrationResult.builder()
                .originalPrice(originalPrice)
                .promotionDiscount(promotionDiscount)
                .priceAfterPromotion(priceAfterPromotion)
                .promotionDetails(promotedCart.getReductionDetails())
                .couponResult(couponResult)
                .productCouponDiscount(productCouponDiscount)
                .shippingCouponDiscount(shippingCouponDiscount)
                .productPayPrice(productPayPrice)
                .shippingFee(shippingFee)
                .shippingPayPrice(shippingPayPrice)
                .totalPayPrice(totalPayPrice)
                .build();
    }
}
