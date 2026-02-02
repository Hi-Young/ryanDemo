package com.ryan.es.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 教学用的样本数据 —— 模拟优惠券.
 */
public class PatternSampleData {

    private PatternSampleData() {
    }

    public static List<PatternDoc> samples() {
        long now = System.currentTimeMillis();
        long day = 86400_000L;

        List<PatternDoc> list = new ArrayList<>();

        list.add(PatternDoc.builder()
                .couponId(1001L)
                .title("满100减20优惠券")
                .couponCategory(1)
                .couponFrom(1)
                .discountAmount(20.0)
                .thresholdAmount(100.0)
                .receiveCount(5280L)
                .startTime(now - 7 * day)
                .endTime(now + 30 * day)
                .status("active")
                .cityCode("110000")
                .stores(Arrays.asList(
                        PatternDoc.Store.builder().code("S001").name("北京朝阳店").corporationCode("CORP_BJ").build(),
                        PatternDoc.Store.builder().code("S002").name("北京海淀店").corporationCode("CORP_BJ").build()
                ))
                .build());

        list.add(PatternDoc.builder()
                .couponId(1002L)
                .title("新人专享满50减10")
                .couponCategory(1)
                .couponFrom(1)
                .discountAmount(10.0)
                .thresholdAmount(50.0)
                .receiveCount(12300L)
                .startTime(now - 3 * day)
                .endTime(now + 60 * day)
                .status("active")
                .cityCode("310000")
                .stores(Arrays.asList(
                        PatternDoc.Store.builder().code("S003").name("上海浦东店").corporationCode("CORP_SH").build(),
                        PatternDoc.Store.builder().code("S004").name("上海徐汇店").corporationCode("CORP_SH").build()
                ))
                .build());

        list.add(PatternDoc.builder()
                .couponId(1003L)
                .title("周末特惠8折券")
                .couponCategory(2)
                .couponFrom(2)
                .discountAmount(0.0)
                .thresholdAmount(0.0)
                .receiveCount(890L)
                .startTime(now - 1 * day)
                .endTime(now + 14 * day)
                .status("active")
                .cityCode("110000")
                .stores(Collections.singletonList(
                        PatternDoc.Store.builder().code("S001").name("北京朝阳店").corporationCode("CORP_BJ").build()
                ))
                .build());

        list.add(PatternDoc.builder()
                .couponId(1004L)
                .title("运费减免券")
                .couponCategory(3)
                .couponFrom(1)
                .discountAmount(8.0)
                .thresholdAmount(0.0)
                .receiveCount(45000L)
                .startTime(now - 10 * day)
                .endTime(now + 20 * day)
                .status("active")
                .cityCode("440100")
                .stores(Arrays.asList(
                        PatternDoc.Store.builder().code("S005").name("广州天河店").corporationCode("CORP_GZ").build(),
                        PatternDoc.Store.builder().code("S001").name("北京朝阳店").corporationCode("CORP_BJ").build()
                ))
                .build());

        list.add(PatternDoc.builder()
                .couponId(1005L)
                .title("满200减50大额优惠券")
                .couponCategory(1)
                .couponFrom(3)
                .discountAmount(50.0)
                .thresholdAmount(200.0)
                .receiveCount(3200L)
                .startTime(now - 30 * day)
                .endTime(now - 1 * day)
                .status("expired")
                .cityCode("110000")
                .stores(Collections.singletonList(
                        PatternDoc.Store.builder().code("S002").name("北京海淀店").corporationCode("CORP_BJ").build()
                ))
                .build());

        list.add(PatternDoc.builder()
                .couponId(1006L)
                .title("会员日满减优惠")
                .couponCategory(1)
                .couponFrom(1)
                .discountAmount(30.0)
                .thresholdAmount(150.0)
                .receiveCount(7800L)
                .startTime(now - 2 * day)
                .endTime(now + 5 * day)
                .status("active")
                .cityCode("310000")
                .stores(Arrays.asList(
                        PatternDoc.Store.builder().code("S003").name("上海浦东店").corporationCode("CORP_SH").build(),
                        PatternDoc.Store.builder().code("S005").name("广州天河店").corporationCode("CORP_GZ").build()
                ))
                .build());

        return list;
    }
}
