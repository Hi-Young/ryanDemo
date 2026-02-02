package com.aeon.demo.dto;

import java.math.BigDecimal;

/**
 * 购物车商品行入参。
 *
 * @author codex
 */
public class CartItemRequest {

    private String cartItemId;
    private String skuId;
    private int quantity;
    private BigDecimal salePrice;

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }
}

