package com.ryan.es.coupon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ryan.es.config.EsDemoProperties;
import com.ryan.es.http.EsHttpResponse;
import com.ryan.es.http.EsRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class CouponEsService {

    private final EsRestClient esRestClient;
    private final EsDemoProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouponEsQueryBuilder queryBuilder = new CouponEsQueryBuilder();

    public CouponEsService(EsRestClient esRestClient, EsDemoProperties properties) {
        this.esRestClient = esRestClient;
        this.properties = properties;
    }

    public EsHttpResponse ping() {
        return esRestClient.get("/");
    }

    public EsHttpResponse createCouponIndex(boolean recreate) {
        String index = properties.getCouponIndex();
        if (!StringUtils.hasText(index)) {
            return new EsHttpResponse(400, "demo.elasticsearch.coupon-index is blank");
        }

        EsHttpResponse head = esRestClient.head("/" + index);
        boolean exists = head.is2xx();

        if (exists && !recreate) {
            return new EsHttpResponse(200, "{\"message\":\"index already exists\",\"index\":\"" + index + "\"}");
        }
        if (exists) {
            esRestClient.delete("/" + index);
        }

        String preset = properties.getCouponIndexMapping() == null
                ? "standard"
                : properties.getCouponIndexMapping().toLowerCase(Locale.ROOT);
        String mappingPath = "es/coupon/coupon-index-standard.json";
        if ("ik".equals(preset)) {
            mappingPath = "es/coupon/coupon-index-ik.json";
        }

        String body;
        try {
            body = loadClasspath(mappingPath);
        } catch (IOException e) {
            return new EsHttpResponse(500, "Failed to load mapping: " + mappingPath + " - " + e.getMessage());
        }

        return esRestClient.putJson("/" + index, body);
    }

    public EsHttpResponse deleteCouponIndex() {
        return esRestClient.delete("/" + properties.getCouponIndex());
    }

    public EsHttpResponse refreshCouponIndex() {
        return esRestClient.postJson("/" + properties.getCouponIndex() + "/_refresh", "{}");
    }

    public EsHttpResponse bulkIndexSamples(boolean refresh) {
        return bulkIndex(CouponEsSampleData.samples(), refresh);
    }

    public EsHttpResponse bulkIndex(List<CouponEsDoc> docs, boolean refresh) {
        if (docs == null || docs.isEmpty()) {
            return new EsHttpResponse(400, "docs is empty");
        }

        String index = properties.getCouponIndex();
        StringBuilder ndjson = new StringBuilder(1024);
        try {
            for (CouponEsDoc doc : docs) {
                if (doc == null || doc.getCouponId() == null) {
                    continue;
                }
                ObjectNode action = objectMapper.createObjectNode();
                action.putObject("index")
                        .put("_index", index)
                        .put("_id", String.valueOf(doc.getCouponId()));
                ndjson.append(objectMapper.writeValueAsString(action)).append('\n');
                ndjson.append(objectMapper.writeValueAsString(doc)).append('\n');
            }
        } catch (Exception e) {
            return new EsHttpResponse(500, "Failed to build bulk ndjson: " + e.getMessage());
        }

        String path = "/_bulk" + (refresh ? "?refresh=true" : "");
        return esRestClient.postNdjson(path, ndjson.toString());
    }

    public EsHttpResponse upsert(CouponEsDoc doc, boolean refresh) {
        if (doc == null || doc.getCouponId() == null) {
            return new EsHttpResponse(400, "couponId is required");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.set("doc", objectMapper.valueToTree(doc));
        body.put("doc_as_upsert", true);

        String path = "/" + properties.getCouponIndex() + "/_update/" + doc.getCouponId()
                + (refresh ? "?refresh=wait_for" : "");
        try {
            return esRestClient.postJson(path, objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return new EsHttpResponse(500, "Failed to build update json: " + e.getMessage());
        }
    }

    public EsHttpResponse getById(Long couponId) {
        if (couponId == null) {
            return new EsHttpResponse(400, "couponId is required");
        }
        return esRestClient.get("/" + properties.getCouponIndex() + "/_doc/" + couponId);
    }

    public EsHttpResponse deleteById(Long couponId) {
        if (couponId == null) {
            return new EsHttpResponse(400, "couponId is required");
        }
        return esRestClient.delete("/" + properties.getCouponIndex() + "/_doc/" + couponId);
    }

    /**
     * Partial update: only update specified fields of a document (AEON-style updateByDoc).
     */
    public EsHttpResponse partialUpdate(Long couponId, Map<String, Object> fields, boolean refresh) {
        if (couponId == null) {
            return new EsHttpResponse(400, "couponId is required");
        }
        if (fields == null || fields.isEmpty()) {
            return new EsHttpResponse(400, "fields is empty");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.set("doc", objectMapper.valueToTree(fields));

        String path = "/" + properties.getCouponIndex() + "/_update/" + couponId
                + (refresh ? "?refresh=wait_for" : "");
        try {
            return esRestClient.postJson(path, objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return new EsHttpResponse(500, "Failed to build partial update json: " + e.getMessage());
        }
    }

    /**
     * Build advanced search DSL (for learning/preview, no ES call).
     */
    public String buildAdvancedDsl(CouponSearchRequest req) {
        return queryBuilder.buildAdvancedSearchBody(req);
    }

    /**
     * Execute advanced search using ChineseSearchStrategy + nested query.
     */
    public CouponSearchResponse advancedSearch(CouponSearchRequest req) {
        String dsl = queryBuilder.buildAdvancedSearchBody(req);
        EsHttpResponse resp = esRestClient.postJson("/" + properties.getCouponIndex() + "/_search", dsl);
        if (!resp.is2xx()) {
            throw new IllegalStateException("ES advanced search failed: " + resp.getStatusCode() + " - " + resp.getBody());
        }
        return parseSearchResponse(resp.getBody(), req);
    }

    public String buildDsl(CouponSearchRequest req) {
        return queryBuilder.buildSearchBody(req);
    }

    public EsHttpResponse searchRaw(CouponSearchRequest req) {
        String dsl = queryBuilder.buildSearchBody(req);
        return esRestClient.postJson("/" + properties.getCouponIndex() + "/_search", dsl);
    }

    public CouponSearchResponse searchParsed(CouponSearchRequest req) {
        EsHttpResponse resp = searchRaw(req);
        if (!resp.is2xx()) {
            throw new IllegalStateException("ES search failed: " + resp.getStatusCode() + " - " + resp.getBody());
        }
        return parseSearchResponse(resp.getBody(), req);
    }

    public List<String> suggestParsed(String prefix, int size) {
        if (!StringUtils.hasText(prefix)) {
            return Collections.emptyList();
        }

        String body = queryBuilder.buildSuggestBody(prefix, size);
        EsHttpResponse resp = esRestClient.postJson("/" + properties.getCouponIndex() + "/_search", body);
        if (!resp.is2xx()) {
            throw new IllegalStateException("ES suggest failed: " + resp.getStatusCode() + " - " + resp.getBody());
        }

        try {
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode suggest = root.path("suggest").path("title_suggest");
            if (!suggest.isArray() || suggest.isEmpty()) {
                return Collections.emptyList();
            }
            JsonNode options = suggest.get(0).path("options");
            if (!options.isArray()) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (JsonNode option : options) {
                String text = option.path("text").asText(null);
                if (StringUtils.hasText(text)) {
                    result.add(text);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse suggest response: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private CouponSearchResponse parseSearchResponse(String body, CouponSearchRequest req) {
        try {
            JsonNode root = objectMapper.readTree(body);
            CouponSearchResponse resp = new CouponSearchResponse();
            resp.setTookMs(root.path("took").asInt(0));

            JsonNode hitsNode = root.path("hits");
            resp.setTotal(extractTotal(hitsNode.path("total")));

            JsonNode hits = hitsNode.path("hits");
            if (hits.isArray()) {
                for (JsonNode hit : hits) {
                    JsonNode src = hit.path("_source");
                    CouponSearchHit one = new CouponSearchHit();
                    one.setCouponId(src.path("couponId").isMissingNode() ? null : src.path("couponId").asLong());
                    one.setTitle(src.path("title").asText(null));
                    one.setMerchantId(src.path("merchantId").isMissingNode() ? null : src.path("merchantId").asLong());
                    one.setMerchantName(src.path("merchantName").asText(null));
                    if (!src.path("discountAmount").isMissingNode()) {
                        one.setDiscountAmount(src.path("discountAmount").asDouble());
                    }
                    if (!src.path("endTime").isMissingNode()) {
                        one.setEndTime(src.path("endTime").asLong());
                    }
                    if (!src.path("weight").isMissingNode()) {
                        one.setWeight(src.path("weight").asInt());
                    }

                    JsonNode hlTitle = hit.path("highlight").path("title");
                    if (hlTitle.isArray() && hlTitle.size() > 0) {
                        one.setHighlightTitle(hlTitle.get(0).asText());
                    }

                    // When sort=distance, ES returns the distance in hit.sort[0].
                    if (req != null
                            && "distance".equalsIgnoreCase(req.getSort())
                            && req.getLat() != null
                            && req.getLon() != null) {
                        JsonNode sort = hit.path("sort");
                        if (sort.isArray() && sort.size() > 0) {
                            one.setDistanceKm(sort.get(0).asDouble());
                        }
                    }

                    resp.getHits().add(one);
                }
            }

            JsonNode aggs = root.path("aggregations");
            fillAggBuckets(aggs.path("by_merchant").path("buckets"), resp.getAggMerchant());
            fillAggBuckets(aggs.path("by_city").path("buckets"), resp.getAggCity());
            fillAggBuckets(aggs.path("by_category").path("buckets"), resp.getAggCategory());

            return resp;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse ES response: " + e.getMessage(), e);
        }
    }

    private long extractTotal(JsonNode totalNode) {
        // ES 7+: {"value":123,"relation":"eq"}; ES 6: 123
        if (totalNode == null || totalNode.isMissingNode()) {
            return 0L;
        }
        if (totalNode.isObject()) {
            return totalNode.path("value").asLong(0L);
        }
        return totalNode.asLong(0L);
    }

    private void fillAggBuckets(JsonNode buckets, List<CouponSearchAggBucket> out) {
        if (!buckets.isArray()) {
            return;
        }
        Iterator<JsonNode> it = buckets.elements();
        while (it.hasNext()) {
            JsonNode b = it.next();
            String key = b.path("key").asText(null);
            long docCount = b.path("doc_count").asLong(0L);
            if (key != null) {
                out.add(new CouponSearchAggBucket(key, docCount));
            }
        }
    }

    private String loadClasspath(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
