package com.ryan.es.coupon;

import lombok.Data;

import java.util.List;

@Data
public class CouponSearchRequest {

    /**
     * Full-text keyword, e.g. "星巴克 买一送一".
     */
    private String keyword;

    private Long merchantId;

    private String cityCode;

    private List<String> categoryIds;

    private List<String> tags;

    /**
     * Filter to show only currently-valid coupons: startTime <= now <= endTime.
     */
    private Boolean validOnly = true;

    private Double minDiscountAmount;

    private Double maxDiscountAmount;

    /**
     * default|discount|expire|sales|distance
     */
    private String sort = "default";

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    /**
     * For geo filtering/sorting (optional).
     */
    private Double lat;

    private Double lon;

    /**
     * Example: 5 means within 5km.
     */
    private Double distanceKm;

    private Boolean highlight = true;

    private Boolean withAggs = true;

    /**
     * Nested store code filter (uses nested query on stores.code).
     */
    private String storeCode;

    /**
     * Corporation code filter (uses nested query on stores.corporationCode).
     */
    private String corporationCode;

    /**
     * Coupon source filter.
     */
    private Integer couponFrom;

    /**
     * Coupon category filter.
     */
    private Integer couponCategory;

    /**
     * Whether to use advanced Chinese search strategy (DisMax + prefix + fuzzy).
     * Default false for backward compatibility.
     */
    private Boolean useAdvancedSearch = false;
}

