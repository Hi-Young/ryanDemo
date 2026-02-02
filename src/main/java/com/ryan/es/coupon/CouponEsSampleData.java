package com.ryan.es.coupon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sample coupon documents used by the ES demo endpoints.
 */
public final class CouponEsSampleData {

    private CouponEsSampleData() {
    }

    public static List<CouponEsDoc> samples() {
        long now = System.currentTimeMillis();
        long oneDay = 24L * 60 * 60 * 1000;

        CouponEsDoc doc1 = CouponEsDoc.builder()
                .couponId(1001L)
                .title("星巴克 买一送一")
                .subTitle("下午茶特惠，限时领取")
                .merchantId(2001L)
                .merchantName("Starbucks")
                .categoryIds(Arrays.asList("food", "coffee"))
                .cityCode("310000")
                .platform("APP")
                .couponType("BUY_ONE_GET_ONE")
                .status("ACTIVE")
                .discountAmount(30.0)
                .thresholdAmount(0.0)
                .weight(80)
                .sales(15234L)
                .startTime(now - oneDay)
                .endTime(now + 30 * oneDay)
                .updateTime(now)
                .location(CouponEsDoc.GeoPoint.builder().lat(31.2304).lon(121.4737).build())
                .tags(Arrays.asList("咖啡", "下午茶", "买一送一"))
                .tagsText("咖啡 下午茶 买一送一")
                .titleSuggest(CouponEsDoc.Completion.builder()
                        .input(Arrays.asList("星巴克", "星巴克买一送一", "下午茶特惠"))
                        .weight(10)
                        .build())
                .couponFrom(1)
                .couponCategory(3)
                .stores(Arrays.asList(
                        CouponEsDoc.Store.builder().code("SH001").name("星巴克南京路店").corporationCode("CORP01").build(),
                        CouponEsDoc.Store.builder().code("SH002").name("星巴克陆家嘴店").corporationCode("CORP01").build()
                ))
                .build();

        CouponEsDoc doc2 = CouponEsDoc.builder()
                .couponId(1002L)
                .title("麦当劳 50-10 满减券")
                .subTitle("早餐/正餐通用")
                .merchantId(2002L)
                .merchantName("McDonald's")
                .categoryIds(Arrays.asList("food", "fastfood"))
                .cityCode("110000")
                .platform("APP")
                .couponType("FULL_REDUCE")
                .status("ACTIVE")
                .discountAmount(10.0)
                .thresholdAmount(50.0)
                .weight(90)
                .sales(52341L)
                .startTime(now - 3 * oneDay)
                .endTime(now + 15 * oneDay)
                .updateTime(now - 2 * 60 * 60 * 1000)
                .location(CouponEsDoc.GeoPoint.builder().lat(39.9042).lon(116.4074).build())
                .tags(Arrays.asList("满减", "早餐"))
                .tagsText("满减 早餐")
                .titleSuggest(CouponEsDoc.Completion.builder()
                        .input(Arrays.asList("麦当劳", "满减券", "50-10"))
                        .weight(9)
                        .build())
                .couponFrom(1)
                .couponCategory(1)
                .stores(Arrays.asList(
                        CouponEsDoc.Store.builder().code("BJ001").name("麦当劳王府井店").corporationCode("CORP02").build(),
                        CouponEsDoc.Store.builder().code("BJ002").name("麦当劳西单店").corporationCode("CORP02").build(),
                        CouponEsDoc.Store.builder().code("SH003").name("麦当劳南京路店").corporationCode("CORP02").build()
                ))
                .build();

        CouponEsDoc doc3 = CouponEsDoc.builder()
                .couponId(1003L)
                .title("优衣库 9折券")
                .subTitle("全场可用（部分商品除外）")
                .merchantId(3001L)
                .merchantName("UNIQLO")
                .categoryIds(Arrays.asList("retail", "clothing"))
                .cityCode("440100")
                .platform("H5")
                .couponType("DISCOUNT")
                .status("ACTIVE")
                .discountAmount(0.9)
                .thresholdAmount(0.0)
                .weight(60)
                .sales(2345L)
                .startTime(now - 2 * oneDay)
                .endTime(now + 45 * oneDay)
                .updateTime(now - 6 * 60 * 60 * 1000)
                .location(CouponEsDoc.GeoPoint.builder().lat(23.1291).lon(113.2644).build())
                .tags(Arrays.asList("服饰", "折扣"))
                .tagsText("服饰 折扣")
                .titleSuggest(CouponEsDoc.Completion.builder()
                        .input(Arrays.asList("优衣库", "折扣券", "9折"))
                        .weight(6)
                        .build())
                .couponFrom(2)
                .couponCategory(2)
                .stores(Arrays.asList(
                        CouponEsDoc.Store.builder().code("GZ001").name("优衣库天河城店").corporationCode("CORP03").build()
                ))
                .build();

        CouponEsDoc doc4 = CouponEsDoc.builder()
                .couponId(1004L)
                .title("电影票 2张立减20")
                .subTitle("周末专享")
                .merchantId(4001L)
                .merchantName("猫眼电影")
                .categoryIds(Arrays.asList("life", "entertainment"))
                .cityCode("310000")
                .platform("APP")
                .couponType("FULL_REDUCE")
                .status("ACTIVE")
                .discountAmount(20.0)
                .thresholdAmount(0.0)
                .weight(70)
                .sales(9876L)
                .startTime(now - oneDay)
                .endTime(now + 7 * oneDay)
                .updateTime(now - 30 * 60 * 1000)
                .location(CouponEsDoc.GeoPoint.builder().lat(31.2200).lon(121.4800).build())
                .tags(Arrays.asList("电影", "周末"))
                .tagsText("电影 周末")
                .titleSuggest(CouponEsDoc.Completion.builder()
                        .input(Arrays.asList("电影票", "立减", "周末专享"))
                        .weight(7)
                        .build())
                .couponFrom(1)
                .couponCategory(1)
                .stores(Arrays.asList(
                        CouponEsDoc.Store.builder().code("SH004").name("猫眼电影环球港店").corporationCode("CORP04").build(),
                        CouponEsDoc.Store.builder().code("SH005").name("猫眼电影五角场店").corporationCode("CORP04").build()
                ))
                .build();

        CouponEsDoc doc5 = CouponEsDoc.builder()
                .couponId(1005L)
                .title("健身月卡 立减50")
                .subTitle("新用户专享")
                .merchantId(5001L)
                .merchantName("超级猩猩")
                .categoryIds(Arrays.asList("life", "fitness"))
                .cityCode("110000")
                .platform("APP")
                .couponType("FULL_REDUCE")
                .status("ACTIVE")
                .discountAmount(50.0)
                .thresholdAmount(0.0)
                .weight(55)
                .sales(1234L)
                .startTime(now - 5 * oneDay)
                .endTime(now + 20 * oneDay)
                .updateTime(now - oneDay)
                .location(CouponEsDoc.GeoPoint.builder().lat(39.9100).lon(116.4000).build())
                .tags(Arrays.asList("健身", "新用户"))
                .tagsText("健身 新用户")
                .titleSuggest(CouponEsDoc.Completion.builder()
                        .input(Arrays.asList("健身", "月卡", "新用户专享"))
                        .weight(5)
                        .build())
                .couponFrom(3)
                .couponCategory(1)
                .stores(Arrays.asList(
                        CouponEsDoc.Store.builder().code("BJ003").name("超级猩猩国贸店").corporationCode("CORP05").build()
                ))
                .build();

        CouponEsDoc doc6 = CouponEsDoc.builder()
                .couponId(1006L)
                .title("水果外卖 满30减8")
                .subTitle("当日达")
                .merchantId(6001L)
                .merchantName("盒马鲜生")
                .categoryIds(Arrays.asList("food", "fresh"))
                .cityCode("310000")
                .platform("APP")
                .couponType("FULL_REDUCE")
                .status("ACTIVE")
                .discountAmount(8.0)
                .thresholdAmount(30.0)
                .weight(65)
                .sales(7654L)
                .startTime(now - 10 * oneDay)
                .endTime(now + 10 * oneDay)
                .updateTime(now - 3 * 60 * 60 * 1000)
                .location(CouponEsDoc.GeoPoint.builder().lat(31.2100).lon(121.4300).build())
                .tags(Arrays.asList("外卖", "生鲜", "满减"))
                .tagsText("外卖 生鲜 满减")
                .titleSuggest(CouponEsDoc.Completion.builder()
                        .input(Arrays.asList("盒马", "生鲜", "满减"))
                        .weight(8)
                        .build())
                .couponFrom(1)
                .couponCategory(1)
                .stores(Arrays.asList(
                        CouponEsDoc.Store.builder().code("SH006").name("盒马鲜生金桥店").corporationCode("CORP06").build(),
                        CouponEsDoc.Store.builder().code("SH007").name("盒马鲜生古北店").corporationCode("CORP06").build(),
                        CouponEsDoc.Store.builder().code("SH001").name("盒马鲜生南京路店").corporationCode("CORP06").build()
                ))
                .build();

        return Collections.unmodifiableList(Arrays.asList(doc1, doc2, doc3, doc4, doc5, doc6));
    }
}

