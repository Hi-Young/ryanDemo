package com.bruce.promotiondemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    /**
     * 商品列表
     */
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    /**
     * 应用的规则列表
     */
    @Builder.Default
    private List<Rule> appliedRules = new ArrayList<>();

    /**
     * 优惠明细列表
     */
    @Builder.Default
    private List<ReductionDetail> reductionDetails = new ArrayList<>();

    /**
     * 获取原始总价
     */
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取应付总价（优惠后）
     */
    public BigDecimal getPayPrice() {
        BigDecimal totalDiscount = getTotalDiscount();
        return getTotalPrice().subtract(totalDiscount);
    }

    /**
     * 获取总优惠金额
     */
    public BigDecimal getTotalDiscount() {
        return reductionDetails.stream()
                .filter(d -> d.getValid())
                .map(ReductionDetail::getReduction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取已应用的规则ID列表
     */
    public List<String> getAppliedRuleIds() {
        return reductionDetails.stream()
                .filter(d -> d.getValid())
                .map(ReductionDetail::getRuleId)
                .collect(Collectors.toList());
    }

    /**
     * 深拷贝
     */
    public Cart copy() {
        return Cart.builder()
                .items(items.stream().map(CartItem::copy).collect(Collectors.toList()))
                .appliedRules(appliedRules.stream().map(Rule::copy).collect(Collectors.toList()))
                .reductionDetails(reductionDetails.stream().map(ReductionDetail::copy).collect(Collectors.toList()))
                .build();
    }
}
