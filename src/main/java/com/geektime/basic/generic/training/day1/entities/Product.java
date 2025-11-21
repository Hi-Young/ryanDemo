package com.geektime.basic.generic.training.day1.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productCode; // 注意：这里主键是String类型
    private String name;
    private BigDecimal price;
    private Integer stock;

    public Product(String name, BigDecimal price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
