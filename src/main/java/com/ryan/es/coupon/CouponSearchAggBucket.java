package com.ryan.es.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponSearchAggBucket {

    private String key;

    private long docCount;
}

