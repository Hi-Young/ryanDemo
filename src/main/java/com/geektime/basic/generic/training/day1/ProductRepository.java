package com.geektime.basic.generic.training.day1;

import com.geektime.basic.generic.training.day1.entities.Product;

import java.util.UUID;

/**
 * 商品Repository实现
 *
 * 🎯 练习任务：继承MemoryRepository，注意Product的主键是String类型
 *
 * 💡 思考：
 * 1. User用Long做主键，Product用String做主键，泛型如何支持这种灵活性？
 * 2. 如果没有泛型，你需要写两套Repository代码吗？
 */
public class ProductRepository extends MemoryRepository<Product, String> {

    @Override
    protected String getId(Product entity) {
        String productCode = entity.getProductCode();
        return productCode;
        // TODO: 返回商品的productCode
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    @Override
    protected void setId(Product entity, String id) {
        entity.setProductCode(id);
        // TODO: 设置商品的productCode
//        throw new UnsupportedOperationException("请实现这个方法");
    }

    /**
     * 生成新的商品编码（UUID）
     */
    protected String generateId() {
        return "P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 🎯 扩展练习：添加一个特有的方法
    // TODO: 实现根据商品名查找商品
    public Product findByName(String name) {
        return storage.values().stream().filter(item->item.getName().equals(name)).findFirst().orElse(null);
//        throw new UnsupportedOperationException("请实现这个方法");
    }
}
