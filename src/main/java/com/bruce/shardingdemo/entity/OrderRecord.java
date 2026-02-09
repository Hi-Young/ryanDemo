package com.bruce.shardingdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单记录（逻辑表：order_record）
 *
 * <p>物理表：order_record_202501 ~ order_record_202512</p>
 * <p>分片键：created_at（按月归档）</p>
 *
 * @author claude code
 */
@Data
@TableName("order_record")
public class OrderRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 订单金额 */
    private BigDecimal amount;

    /** 状态: 0-待支付 1-已支付 2-已取消 */
    private Integer status;

    /** 创建时间 — 分片键 */
    private Date createdAt;

    private Date updatedAt;
}
