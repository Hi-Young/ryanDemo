package com.aeon.demo.domain;

import java.math.BigDecimal;

/**
 * 购物车商品行（内部领域对象）。
 *
 * @author codex
 */
public class CartItem {

    private final String cartItemId;
    private final String skuId;
    private final int quantity;
    private final BigDecimal salePrice; // 原价(单价)

    public CartItem(String cartItemId, String skuId, int quantity, BigDecimal salePrice) {
        this.cartItemId = cartItemId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.salePrice = salePrice;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public String getSkuId() {
        return skuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }
}

