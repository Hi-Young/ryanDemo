package com.bruce.shardingdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 券聚合记录（逻辑表：coupon_give_record_gather）
 *
 * <p>物理表：coupon_give_record_gather_00 ~ coupon_give_record_gather_03</p>
 * <p>分片键：coupon_template_id</p>
 *
 * @author claude code
 */
@Data
@TableName("coupon_give_record_gather")
public class CouponGiveRecordGather {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 券模板ID — 分片键 */
    private Long couponTemplateId;

    /** 会员ID */
    private Long memberId;

    /** 券编号 */
    private String couponNo;

    /** 状态: 0-未使用 1-已使用 2-已过期 */
    private Integer status;

    /** 发放数量 */
    private Integer giveCount;

    private Date createdAt;

    private Date updatedAt;
}
