package com.ryan.es.patterns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教学用的 ES 文档模型 —— 模拟优惠券.
 *
 * 对应 coupon-search 中的 IndexDeliveryStoreCouponData，
 * 但只保留最能说明问题的字段，降低学习噪音.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternDoc {

    /** 券 ID（当作 ES 文档的 _id） */
    private Long couponId;

    /** 券标题（全文搜索字段） */
    private String title;

    /** 券分类: 1=满减券, 2=折扣券, 3=运费券 */
    private Integer couponCategory;

    /** 券来源: 1=平台, 2=商户, 3=品牌 */
    private Integer couponFrom;

    /** 优惠金额 */
    private Double discountAmount;

    /** 使用门槛金额 */
    private Double thresholdAmount;

    /** 累计领取数 */
    private Long receiveCount;

    /** 生效时间 (epoch millis) */
    private Long startTime;

    /** 失效时间 (epoch millis) */
    private Long endTime;

    /** 状态: "active" / "expired" */
    private String status;

    /** 城市编码 */
    private String cityCode;

    /**
     * 适用门店列表（嵌套对象）.
     *
     * coupon-search 中把门店作为 nested 类型存储，
     * 这样可以对门店内部字段做独立查询，不会跨门店"串联"匹配.
     */
    private List<Store> stores;

    /**
     * 嵌套的门店对象.
     *
     * nested 类型的关键点：每个 Store 在内部是独立的 Lucene 文档，
     * 查询时必须用 nested query 才能正确匹配.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Store {
        /** 门店编码 */
        private String code;
        /** 门店名称 */
        private String name;
        /** 法人编码 */
        private String corporationCode;
    }
}
