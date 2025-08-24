package com.ryan.deadlock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存表实体类 - 用于模拟库存扣减死锁场景
 */
@Data
@TableName("inventory")
public class Inventory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("product_id")
    private Integer productId;
    
    @TableField("product_name")
    private String productName;
    
    @TableField("stock_quantity")
    private Integer stockQuantity;
    
    @TableField("reserved_quantity")
    private Integer reservedQuantity;
    
    @Version
    @TableField("version")
    private Integer version;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public Inventory() {}
    
    public Inventory(Integer productId, String productName, Integer stockQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = 0;
    }
    
    /**
     * 获取可用库存数量
     */
    public Integer getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }
}