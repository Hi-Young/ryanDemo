package com.bruce.coupondemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * 券实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    /** 券ID */
    private String couponId;
    /** 券名称 */
    private String name;
    /** 券类型 */
    private CouponType couponType;
    /** 面值 */
    private BigDecimal faceValue;
    /** 使用门槛金额（null或0表示无门槛） */
    private BigDecimal threshold;
    /** 适用SKU编码集合（null或空表示全场通用） */
    private Set<String> applicableSkuCodes;
    /** 过期日期 */
    private LocalDate expireDate;
    /** 互斥组ID（同组最多选1张，null表示无互斥） */
    private String exclusiveGroupId;
    /** 是否被选中 */
    @Builder.Default
    private Boolean checked = false;
    /** 实际抵扣金额（选中后计算） */
    private BigDecimal actualDiscount;

    public Coupon copy() {
        return Coupon.builder()
                .couponId(couponId)
                .name(name)
                .couponType(couponType)
                .faceValue(faceValue)
                .threshold(threshold)
                .applicableSkuCodes(applicableSkuCodes)
                .expireDate(expireDate)
                .exclusiveGroupId(exclusiveGroupId)
                .checked(checked)
                .actualDiscount(actualDiscount)
                .build();
    }
}
