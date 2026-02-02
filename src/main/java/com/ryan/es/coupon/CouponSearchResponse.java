package com.ryan.es.coupon;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CouponSearchResponse {

    private int tookMs;

    private long total;

    private List<CouponSearchHit> hits = new ArrayList<>();

    private List<CouponSearchAggBucket> aggMerchant = new ArrayList<>();

    private List<CouponSearchAggBucket> aggCity = new ArrayList<>();

    private List<CouponSearchAggBucket> aggCategory = new ArrayList<>();
}

