package com.bruce.promotiondemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReductionDetail {

    /**
     * 规则ID
     */
    private String ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 计算基准价（该规则执行前的价格）
     */
    private BigDecimal basePrice;

    /**
     * 减免金额
     */
    private BigDecimal reduction;

    /**
     * 计算后价格
     */
    private BigDecimal calculatedPrice;

    /**
     * 是否有效（回滚后变为无效）
     */
    @Builder.Default
    private Boolean valid = true;

    /**
     * 深拷贝
     */
    public ReductionDetail copy() {
        return ReductionDetail.builder()
                .ruleId(this.ruleId)
                .ruleName(this.ruleName)
                .basePrice(this.basePrice)
                .reduction(this.reduction)
                .calculatedPrice(this.calculatedPrice)
                .valid(this.valid)
                .build();
    }
}
