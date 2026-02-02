package com.bruce.coupondemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 券计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResult {
    /** 所有可用券（含checked标记） */
    @Builder.Default
    private List<Coupon> availableCoupons = new ArrayList<>();
    /** 商品券总优惠 */
    @Builder.Default
    private BigDecimal productCouponDiscount = BigDecimal.ZERO;
    /** 运费券总优惠 */
    @Builder.Default
    private BigDecimal shippingCouponDiscount = BigDecimal.ZERO;

    /** 券总优惠 = 商品券优惠 + 运费券优惠 */
    public BigDecimal getTotalCouponDiscount() {
        return productCouponDiscount.add(shippingCouponDiscount);
    }
}
