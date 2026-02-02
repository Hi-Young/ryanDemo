package com.ryan.es.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Coupon document stored in Elasticsearch.
 *
 * <p>This is intentionally "resume-friendly": contains the typical fields used by a coupon search service
 * (full-text, filters, rank weight, geo, suggest).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponEsDoc {

    private Long couponId;

    private String title;

    private String subTitle;

    private Long merchantId;

    private String merchantName;

    /**
     * Facet/filter field.
     */
    private List<String> categoryIds;

    private String cityCode;

    private String platform;

    private String couponType;

    private String status;

    private Double discountAmount;

    private Double thresholdAmount;

    /**
     * Operation ranking weight (运营权重).
     */
    private Integer weight;

    private Long sales;

    /**
     * Coupon validity (epoch millis).
     */
    private Long startTime;

    private Long endTime;

    private Long updateTime;

    /**
     * Geo search/sort.
     */
    private GeoPoint location;

    /**
     * Tags for exact filtering + a text field for full-text search.
     */
    private List<String> tags;

    private String tagsText;

    /**
     * Nested store list for store-level filtering (AEON-style).
     */
    private List<Store> stores;

    /**
     * Coupon source type (e.g. 1=platform, 2=merchant, 3=brand).
     */
    private Integer couponFrom;

    /**
     * Coupon category (e.g. 1=full_reduce, 2=discount, 3=gift).
     */
    private Integer couponCategory;

    /**
     * Auto-complete suggestions based on coupon title / merchant name.
     */
    private Completion titleSuggest;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Store {
        private String code;
        private String name;
        private String corporationCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPoint {
        private Double lat;
        private Double lon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Completion {
        private List<String> input;
        private Integer weight;
    }
}

