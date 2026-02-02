package com.ryan.es.coupon;

import lombok.Data;

@Data
public class CouponSearchHit {

    private Long couponId;

    private String title;

    private String highlightTitle;

    private Long merchantId;

    private String merchantName;

    private Double discountAmount;

    private Long endTime;

    private Integer weight;

    /**
     * Distance (km) when sorting by geo distance (from ES hit.sort[0]).
     */
    private Double distanceKm;
}

