package com.bruce.coupondemo.model;

import com.bruce.promotiondemo.model.ReductionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 编排结果 - 促销+券联动的最终计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationResult {
    /** 商品原价 */
    private BigDecimal originalPrice;
    /** 促销优惠金额 */
    @Builder.Default
    private BigDecimal promotionDiscount = BigDecimal.ZERO;
    /** 促销后价格 */
    private BigDecimal priceAfterPromotion;
    /** 促销优惠明细 */
    @Builder.Default
    private List<ReductionDetail> promotionDetails = new ArrayList<>();
    /** 券计算结果 */
    private CouponResult couponResult;
    /** 商品券优惠金额 */
    @Builder.Default
    private BigDecimal productCouponDiscount = BigDecimal.ZERO;
    /** 运费券优惠金额 */
    @Builder.Default
    private BigDecimal shippingCouponDiscount = BigDecimal.ZERO;
    /** 商品应付金额（促销后价 - 商品券优惠） */
    private BigDecimal productPayPrice;
    /** 原始运费 */
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;
    /** 运费应付金额（运费 - 运费券优惠） */
    private BigDecimal shippingPayPrice;
    /** 最终总应付 = 商品应付 + 运费应付 */
    private BigDecimal totalPayPrice;
}
