package com.geektime.basic.generic.training.day1.answer;

import com.geektime.basic.generic.training.day1.entities.Product;

import java.util.UUID;

/**
 * 参考答案：商品Repository实现
 *
 * ⚠️ 先自己完成练习，再来看这个答案！
 */
public class ProductRepositoryAnswer extends MemoryRepositoryAnswer<Product, String> {

    @Override
    protected String getId(Product entity) {
        return entity.getProductCode();
    }

    @Override
    protected void setId(Product entity, String id) {
        entity.setProductCode(id);
    }

    @Override
    protected String generateId() {
        return "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Product findByName(String name) {
        return storage.values().stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
