package com.ryan.es.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Build ES DSL for coupon search (bool/filter + function_score + highlight + aggs).
 */
public class CouponEsQueryBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildSearchBody(CouponSearchRequest req) {
        ObjectNode root = objectMapper.createObjectNode();

        int pageNo = req.getPageNo() == null || req.getPageNo() < 1 ? 1 : req.getPageNo();
        int pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 10 : Math.min(req.getPageSize(), 100);
        int from = (pageNo - 1) * pageSize;

        root.put("from", from);
        root.put("size", pageSize);
        root.put("track_total_hits", true);

        // Keep response lightweight (typical online search optimization).
        ArrayNode source = root.putArray("_source");
        source.add("couponId");
        source.add("title");
        source.add("subTitle");
        source.add("merchantId");
        source.add("merchantName");
        source.add("discountAmount");
        source.add("endTime");
        source.add("weight");

        ObjectNode bool = objectMapper.createObjectNode();
        ArrayNode must = bool.putArray("must");
        ArrayNode filter = bool.putArray("filter");

        if (StringUtils.hasText(req.getKeyword())) {
            ObjectNode multiMatch = must.addObject().putObject("multi_match");
            multiMatch.put("query", req.getKeyword());
            ArrayNode fields = multiMatch.putArray("fields");
            fields.add("title^3");
            fields.add("subTitle^2");
            fields.add("merchantName^2");
            fields.add("tagsText");
            multiMatch.put("type", "best_fields");
            multiMatch.put("operator", "and");

            // Exact phrase boost (common in coupon search).
            ObjectNode shouldPhrase = bool.putArray("should")
                    .addObject()
                    .putObject("match_phrase");
            shouldPhrase.putObject("title").put("query", req.getKeyword()).put("boost", 2.0);
            bool.put("minimum_should_match", 0);
        } else {
            must.addObject().putObject("match_all");
        }

        if (req.getMerchantId() != null) {
            filter.addObject().putObject("term").put("merchantId", req.getMerchantId());
        }
        if (StringUtils.hasText(req.getCityCode())) {
            filter.addObject().putObject("term").put("cityCode", req.getCityCode());
        }
        if (!CollectionUtils.isEmpty(req.getCategoryIds())) {
            filter.addObject().putObject("terms").set("categoryIds", objectMapper.valueToTree(req.getCategoryIds()));
        }
        if (!CollectionUtils.isEmpty(req.getTags())) {
            filter.addObject().putObject("terms").set("tags", objectMapper.valueToTree(req.getTags()));
        }

        if (Boolean.TRUE.equals(req.getValidOnly())) {
            filter.addObject()
                    .putObject("range")
                    .putObject("startTime")
                    .put("lte", "now");
            filter.addObject()
                    .putObject("range")
                    .putObject("endTime")
                    .put("gte", "now");
        }

        if (req.getMinDiscountAmount() != null || req.getMaxDiscountAmount() != null) {
            ObjectNode range = filter.addObject().putObject("range").putObject("discountAmount");
            if (req.getMinDiscountAmount() != null) {
                range.put("gte", req.getMinDiscountAmount());
            }
            if (req.getMaxDiscountAmount() != null) {
                range.put("lte", req.getMaxDiscountAmount());
            }
        }

        boolean hasGeo = req.getLat() != null && req.getLon() != null;
        if (hasGeo && req.getDistanceKm() != null && req.getDistanceKm() > 0) {
            ObjectNode geo = filter.addObject().putObject("geo_distance");
            geo.put("distance", req.getDistanceKm() + "km");
            geo.putObject("location")
                    .put("lat", req.getLat())
                    .put("lon", req.getLon());
        }

        // function_score: combine text relevance with operation weight.
        ObjectNode query = root.putObject("query").putObject("function_score");
        query.set("query", objectMapper.createObjectNode().set("bool", bool));
        ArrayNode functions = query.putArray("functions");
        functions.addObject()
                .putObject("field_value_factor")
                .put("field", "weight")
                .put("factor", 0.1)
                .put("missing", 0);
        query.put("score_mode", "sum");
        query.put("boost_mode", "sum");

        // Sorting
        ArrayNode sort = root.putArray("sort");
        String sortType = req.getSort() == null ? "default" : req.getSort().toLowerCase(Locale.ROOT);
        switch (sortType) {
            case "discount":
                sort.addObject().putObject("discountAmount").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "expire":
                sort.addObject().putObject("endTime").put("order", "asc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "sales":
                sort.addObject().putObject("sales").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "distance":
                if (hasGeo) {
                    ObjectNode geoSort = sort.addObject().putObject("_geo_distance");
                    geoSort.putObject("location")
                            .put("lat", req.getLat())
                            .put("lon", req.getLon());
                    geoSort.put("order", "asc");
                    geoSort.put("unit", "km");
                    geoSort.put("distance_type", "arc");
                }
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "default":
            default:
                sort.addObject().putObject("_score").put("order", "desc");
                sort.addObject().putObject("weight").put("order", "desc");
                sort.addObject().putObject("updateTime").put("order", "desc");
                break;
        }

        if (Boolean.TRUE.equals(req.getHighlight())) {
            ObjectNode highlight = root.putObject("highlight");
            highlight.putArray("pre_tags").add("<em>");
            highlight.putArray("post_tags").add("</em>");
            ObjectNode fields = highlight.putObject("fields");
            fields.putObject("title");
            fields.putObject("subTitle");
        }

        if (Boolean.TRUE.equals(req.getWithAggs())) {
            ObjectNode aggs = root.putObject("aggs");
            aggs.putObject("by_merchant").putObject("terms").put("field", "merchantId").put("size", 10);
            aggs.putObject("by_city").putObject("terms").put("field", "cityCode").put("size", 10);
            aggs.putObject("by_category").putObject("terms").put("field", "categoryIds").put("size", 10);
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build ES query JSON", e);
        }
    }

    /**
     * Advanced search body using {@link ChineseSearchStrategy} dis_max + nested store filter.
     * The original {@link #buildSearchBody} is kept unchanged for backward compatibility.
     */
    public String buildAdvancedSearchBody(CouponSearchRequest req) {
        ObjectNode root = objectMapper.createObjectNode();

        int pageNo = req.getPageNo() == null || req.getPageNo() < 1 ? 1 : req.getPageNo();
        int pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 10 : Math.min(req.getPageSize(), 100);
        int from = (pageNo - 1) * pageSize;

        root.put("from", from);
        root.put("size", pageSize);
        root.put("track_total_hits", true);

        ArrayNode source = root.putArray("_source");
        source.add("couponId");
        source.add("title");
        source.add("subTitle");
        source.add("merchantId");
        source.add("merchantName");
        source.add("discountAmount");
        source.add("endTime");
        source.add("weight");
        source.add("stores");
        source.add("couponFrom");
        source.add("couponCategory");

        ObjectNode bool = objectMapper.createObjectNode();
        ArrayNode must = bool.putArray("must");
        ArrayNode filter = bool.putArray("filter");

        // --- Core: use ChineseSearchStrategy for keyword search ---
        if (StringUtils.hasText(req.getKeyword())) {
            ChineseSearchStrategy strategy = new ChineseSearchStrategy(objectMapper);
            ObjectNode disMaxQuery = strategy.buildDisMaxQuery("title", req.getKeyword());
            must.addObject().setAll(disMaxQuery);
        } else {
            must.addObject().putObject("match_all");
        }

        // --- Standard filters (same as basic search) ---
        if (req.getMerchantId() != null) {
            filter.addObject().putObject("term").put("merchantId", req.getMerchantId());
        }
        if (StringUtils.hasText(req.getCityCode())) {
            filter.addObject().putObject("term").put("cityCode", req.getCityCode());
        }
        if (!CollectionUtils.isEmpty(req.getCategoryIds())) {
            filter.addObject().putObject("terms").set("categoryIds", objectMapper.valueToTree(req.getCategoryIds()));
        }
        if (!CollectionUtils.isEmpty(req.getTags())) {
            filter.addObject().putObject("terms").set("tags", objectMapper.valueToTree(req.getTags()));
        }

        // --- New filters: couponFrom, couponCategory ---
        if (req.getCouponFrom() != null) {
            filter.addObject().putObject("term").put("couponFrom", req.getCouponFrom());
        }
        if (req.getCouponCategory() != null) {
            filter.addObject().putObject("term").put("couponCategory", req.getCouponCategory());
        }

        // --- Nested query: filter by stores.code ---
        if (StringUtils.hasText(req.getStoreCode())) {
            ObjectNode nested = filter.addObject().putObject("nested");
            nested.put("path", "stores");
            nested.putObject("query")
                    .putObject("term")
                    .put("stores.code", req.getStoreCode());
        }

        // --- Nested query: filter by stores.corporationCode ---
        if (StringUtils.hasText(req.getCorporationCode())) {
            ObjectNode nested = filter.addObject().putObject("nested");
            nested.put("path", "stores");
            nested.putObject("query")
                    .putObject("term")
                    .put("stores.corporationCode", req.getCorporationCode());
        }

        // --- Time range filter ---
        if (Boolean.TRUE.equals(req.getValidOnly())) {
            filter.addObject()
                    .putObject("range")
                    .putObject("startTime")
                    .put("lte", "now");
            filter.addObject()
                    .putObject("range")
                    .putObject("endTime")
                    .put("gte", "now");
        }

        // --- Discount range filter ---
        if (req.getMinDiscountAmount() != null || req.getMaxDiscountAmount() != null) {
            ObjectNode range = filter.addObject().putObject("range").putObject("discountAmount");
            if (req.getMinDiscountAmount() != null) {
                range.put("gte", req.getMinDiscountAmount());
            }
            if (req.getMaxDiscountAmount() != null) {
                range.put("lte", req.getMaxDiscountAmount());
            }
        }

        // --- Geo filter ---
        boolean hasGeo = req.getLat() != null && req.getLon() != null;
        if (hasGeo && req.getDistanceKm() != null && req.getDistanceKm() > 0) {
            ObjectNode geo = filter.addObject().putObject("geo_distance");
            geo.put("distance", req.getDistanceKm() + "km");
            geo.putObject("location")
                    .put("lat", req.getLat())
                    .put("lon", req.getLon());
        }

        // --- function_score ---
        ObjectNode query = root.putObject("query").putObject("function_score");
        query.set("query", objectMapper.createObjectNode().set("bool", bool));
        ArrayNode functions = query.putArray("functions");
        functions.addObject()
                .putObject("field_value_factor")
                .put("field", "weight")
                .put("factor", 0.1)
                .put("missing", 0);
        query.put("score_mode", "sum");
        query.put("boost_mode", "sum");

        // --- Sorting ---
        ArrayNode sort = root.putArray("sort");
        String sortType = req.getSort() == null ? "default" : req.getSort().toLowerCase(Locale.ROOT);
        switch (sortType) {
            case "discount":
                sort.addObject().putObject("discountAmount").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "expire":
                sort.addObject().putObject("endTime").put("order", "asc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "sales":
                sort.addObject().putObject("sales").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "distance":
                if (hasGeo) {
                    ObjectNode geoSort = sort.addObject().putObject("_geo_distance");
                    geoSort.putObject("location")
                            .put("lat", req.getLat())
                            .put("lon", req.getLon());
                    geoSort.put("order", "asc");
                    geoSort.put("unit", "km");
                    geoSort.put("distance_type", "arc");
                }
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "default":
            default:
                sort.addObject().putObject("_score").put("order", "desc");
                sort.addObject().putObject("weight").put("order", "desc");
                sort.addObject().putObject("updateTime").put("order", "desc");
                break;
        }

        // --- Highlight ---
        if (Boolean.TRUE.equals(req.getHighlight())) {
            ObjectNode highlight = root.putObject("highlight");
            highlight.putArray("pre_tags").add("<em>");
            highlight.putArray("post_tags").add("</em>");
            ObjectNode fields = highlight.putObject("fields");
            fields.putObject("title");
            fields.putObject("title.smart_word");
            fields.putObject("subTitle");
        }

        // --- Aggregations ---
        if (Boolean.TRUE.equals(req.getWithAggs())) {
            ObjectNode aggs = root.putObject("aggs");
            aggs.putObject("by_merchant").putObject("terms").put("field", "merchantId").put("size", 10);
            aggs.putObject("by_city").putObject("terms").put("field", "cityCode").put("size", 10);
            aggs.putObject("by_category").putObject("terms").put("field", "categoryIds").put("size", 10);
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build advanced ES query JSON", e);
        }
    }

    public String buildSuggestBody(String prefix, int size) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("size", 0);

        ObjectNode suggest = root.putObject("suggest");
        ObjectNode entry = suggest.putObject("title_suggest");
        entry.put("prefix", prefix);
        entry.putObject("completion")
                .put("field", "titleSuggest")
                .put("skip_duplicates", true)
                .put("size", size <= 0 ? 10 : Math.min(size, 50));

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build ES suggest JSON", e);
        }
    }
}
