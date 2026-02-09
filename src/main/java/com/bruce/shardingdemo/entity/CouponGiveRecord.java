package com.bruce.shardingdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 会员券发放记录（逻辑表：coupon_give_record）
 *
 * <p>物理表：coupon_give_record_00 ~ coupon_give_record_03</p>
 * <p>分片键：member_id（主）/ coupon_no（备用）</p>
 *
 * @author claude code
 */
@Data
@TableName("coupon_give_record")
public class CouponGiveRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员ID — 主分片键 */
    private Long memberId;

    /** 券编号 — 备用分片键（末8位与 memberId 取模一致） */
    private Long couponNo;

    /** 券模板ID */
    private Long couponTemplateId;

    /** 券名称 */
    private String couponName;

    /** 状态: 0-未使用 1-已使用 2-已过期 */
    private Integer status;

    /** 使用时间 */
    private Date useTime;

    private Date createdAt;

    private Date updatedAt;
}
