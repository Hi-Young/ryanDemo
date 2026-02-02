package com.bruce.promotiondemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车商品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 应付金额（优惠后）
     */
    private BigDecimal payPrice;

    /**
     * 获取商品总价（原价 * 数量）
     */
    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 深拷贝
     */
    public CartItem copy() {
        return CartItem.builder()
                .skuCode(this.skuCode)
                .price(this.price)
                .quantity(this.quantity)
                .payPrice(this.payPrice)
                .build();
    }
}
