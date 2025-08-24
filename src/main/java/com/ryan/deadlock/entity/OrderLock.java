package com.ryan.deadlock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单锁表实体类 - 用于模拟订单处理死锁场景
 */
@Data
@TableName("order_locks")
public class OrderLock {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("order_no")
    private String orderNo;
    
    @TableField("user_id")
    private Integer userId;
    
    @TableField("product_ids")
    private String productIds;
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("status")
    private Integer status;
    
    @TableField("process_time")
    private LocalDateTime processTime;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING(0, "待处理"),
        PROCESSING(1, "处理中"), 
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消");
        
        private final Integer code;
        private final String desc;
        
        OrderStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public Integer getCode() { return code; }
        public String getDesc() { return desc; }
    }
    
    public OrderLock() {}
    
    public OrderLock(String orderNo, Integer userId, List<Integer> productIds, BigDecimal totalAmount) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.productIds = productIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING.getCode();
    }
    
    /**
     * 获取商品ID列表
     */
    public List<Integer> getProductIdList() {
        if (productIds == null || productIds.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.stream(productIds.split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}