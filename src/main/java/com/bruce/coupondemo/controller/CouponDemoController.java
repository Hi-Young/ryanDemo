package com.bruce.coupondemo.controller;

import com.bruce.coupondemo.model.Coupon;
import com.bruce.coupondemo.model.CouponType;
import com.bruce.coupondemo.model.OrchestrationResult;
import com.bruce.coupondemo.service.OrderCalcService;
import com.bruce.promotiondemo.model.CartItem;
import com.bruce.promotiondemo.model.Rule;
import com.bruce.promotiondemo.model.RuleType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 券+促销联动演示 Controller
 * <p>
 * POST /api/coupon-demo/calculate
 */
@RestController
@RequestMapping("/api/coupon-demo")
public class CouponDemoController {

    private final OrderCalcService orderCalcService = new OrderCalcService();

    /**
     * 计算促销+券联动结果
     * <p>
     * 支持自定义入参，也可不传使用默认场景（250元商品 + 满200减30 + 50元券 + 10元运费券）
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody(required = false) Map<String, Object> request) {
        List<CartItem> items;
        List<Rule> rules;
        List<Coupon> coupons;
        BigDecimal shippingFee;

        if (request == null || request.isEmpty()) {
            // 默认场景
            items = createDefaultItems();
            rules = createDefaultRules();
            coupons = createDefaultCoupons();
            shippingFee = new BigDecimal("15");
        } else {
            items = parseItems(request);
            rules = parseRules(request);
            coupons = parseCoupons(request);
            shippingFee = request.containsKey("shippingFee")
                    ? new BigDecimal(request.get("shippingFee").toString())
                    : BigDecimal.ZERO;
        }

        OrchestrationResult result = orderCalcService.calculate(items, rules, coupons, shippingFee);
        return buildResultMap(result);
    }

    // ======================== 默认场景 ========================

    private List<CartItem> createDefaultItems() {
        List<CartItem> items = new ArrayList<>();
        items.add(CartItem.builder().skuCode("SKU001").price(new BigDecimal("100")).quantity(2).build());
        items.add(CartItem.builder().skuCode("SKU002").price(new BigDecimal("50")).quantity(1).build());
        return items;
    }

    private List<Rule> createDefaultRules() {
        List<Rule> rules = new ArrayList<>();
        rules.add(Rule.builder()
                .id("R1").name("满200减30")
                .type(RuleType.THRESHOLD_AMOUNT_OFF)
                .threshold(new BigDecimal("200"))
                .discount(new BigDecimal("30"))
                .priority(1)
                .build());
        return rules;
    }

    private List<Coupon> createDefaultCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(Coupon.builder()
                .couponId("C1").name("50元商品券")
                .couponType(CouponType.PRODUCT)
                .faceValue(new BigDecimal("50"))
                .threshold(new BigDecimal("150"))
                .expireDate(LocalDate.of(2026, 12, 31))
                .build());
        coupons.add(Coupon.builder()
                .couponId("C2").name("10元运费券")
                .couponType(CouponType.SHIPPING)
                .faceValue(new BigDecimal("10"))
                .expireDate(LocalDate.of(2026, 12, 31))
                .build());
        return coupons;
    }

    // ======================== 请求解析 ========================

    @SuppressWarnings("unchecked")
    private List<CartItem> parseItems(Map<String, Object> request) {
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) request.get("items");
        if (itemList == null) {
            return createDefaultItems();
        }
        List<CartItem> items = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
            items.add(CartItem.builder()
                    .skuCode((String) item.get("skuCode"))
                    .price(new BigDecimal(item.get("price").toString()))
                    .quantity(Integer.valueOf(item.get("quantity").toString()))
                    .build());
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<Rule> parseRules(Map<String, Object> request) {
        List<Map<String, Object>> ruleList = (List<Map<String, Object>>) request.get("rules");
        if (ruleList == null) {
            return Collections.emptyList();
        }
        List<Rule> rules = new ArrayList<>();
        for (Map<String, Object> r : ruleList) {
            Rule.RuleBuilder builder = Rule.builder()
                    .id((String) r.get("id"))
                    .name((String) r.get("name"))
                    .type(RuleType.valueOf((String) r.get("type")))
                    .discount(new BigDecimal(r.get("discount").toString()));
            if (r.containsKey("threshold")) {
                builder.threshold(new BigDecimal(r.get("threshold").toString()));
            }
            if (r.containsKey("priority")) {
                builder.priority(Integer.valueOf(r.get("priority").toString()));
            }
            rules.add(builder.build());
        }
        return rules;
    }

    @SuppressWarnings("unchecked")
    private List<Coupon> parseCoupons(Map<String, Object> request) {
        List<Map<String, Object>> couponList = (List<Map<String, Object>>) request.get("coupons");
        if (couponList == null) {
            return Collections.emptyList();
        }
        List<Coupon> coupons = new ArrayList<>();
        for (Map<String, Object> c : couponList) {
            Coupon.CouponBuilder builder = Coupon.builder()
                    .couponId((String) c.get("couponId"))
                    .name((String) c.get("name"))
                    .couponType(CouponType.valueOf((String) c.get("couponType")))
                    .faceValue(new BigDecimal(c.get("faceValue").toString()));
            if (c.containsKey("threshold")) {
                builder.threshold(new BigDecimal(c.get("threshold").toString()));
            }
            if (c.containsKey("expireDate")) {
                builder.expireDate(LocalDate.parse(c.get("expireDate").toString()));
            }
            if (c.containsKey("exclusiveGroupId")) {
                builder.exclusiveGroupId((String) c.get("exclusiveGroupId"));
            }
            if (c.containsKey("applicableSkuCodes")) {
                builder.applicableSkuCodes(new HashSet<>((List<String>) c.get("applicableSkuCodes")));
            }
            coupons.add(builder.build());
        }
        return coupons;
    }

    // ======================== 结果构建 ========================

    private Map<String, Object> buildResultMap(OrchestrationResult result) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("originalPrice", result.getOriginalPrice());
        map.put("promotionDiscount", result.getPromotionDiscount());
        map.put("priceAfterPromotion", result.getPriceAfterPromotion());
        map.put("productCouponDiscount", result.getProductCouponDiscount());
        map.put("shippingCouponDiscount", result.getShippingCouponDiscount());
        map.put("productPayPrice", result.getProductPayPrice());
        map.put("shippingFee", result.getShippingFee());
        map.put("shippingPayPrice", result.getShippingPayPrice());
        map.put("totalPayPrice", result.getTotalPayPrice());

        // 促销明细
        List<Map<String, Object>> promoDetails = new ArrayList<>();
        if (result.getPromotionDetails() != null) {
            result.getPromotionDetails().forEach(d -> {
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("ruleId", d.getRuleId());
                detail.put("ruleName", d.getRuleName());
                detail.put("basePrice", d.getBasePrice());
                detail.put("reduction", d.getReduction());
                detail.put("calculatedPrice", d.getCalculatedPrice());
                detail.put("valid", d.getValid());
                promoDetails.add(detail);
            });
        }
        map.put("promotionDetails", promoDetails);

        // 券明细
        List<Map<String, Object>> couponDetails = new ArrayList<>();
        if (result.getCouponResult() != null && result.getCouponResult().getAvailableCoupons() != null) {
            result.getCouponResult().getAvailableCoupons().forEach(c -> {
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("couponId", c.getCouponId());
                detail.put("name", c.getName());
                detail.put("couponType", c.getCouponType());
                detail.put("faceValue", c.getFaceValue());
                detail.put("checked", c.getChecked());
                detail.put("actualDiscount", c.getActualDiscount());
                couponDetails.add(detail);
            });
        }
        map.put("couponDetails", couponDetails);

        return map;
    }
}
