package com.ryan.es.patterns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ryan.es.http.EsHttpResponse;
import com.ryan.es.http.EsRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 教学用 ES 服务 —— 索引管理 + 文档 CRUD + 各模式的实际执行.
 *
 * <pre>
 * 对应 coupon-search 中的三层调用：
 *   Controller → Service → BaseElasticSearchService / JestClient
 *
 * 这里把 Service 和底层操作合并在一起，减少跳转方便学习.
 * </pre>
 */
@Slf4j
@Service
public class EsPatternService {

    private static final String INDEX_NAME = "pattern_demo_v1";

    private final EsRestClient esRestClient;
    private final ObjectMapper om = new ObjectMapper();
    private final EsQueryPatterns patterns = new EsQueryPatterns();

    public EsPatternService(EsRestClient esRestClient) {
        this.esRestClient = esRestClient;
    }

    // ========== 索引管理（对应 coupon-search 的 CreateIndexTest） ==========

    /**
     * 创建索引 + 设置映射.
     *
     * 对应 coupon-search 中 CreateIndexTest.createIndexTest()
     * 和 CreateIndexMappingTest.createSearchCouponMappingTest() 两步操作.
     *
     * coupon-search 分两步做：
     *   1. createIndex（带 analysis 设置）
     *   2. createIndexMapping（放 mapping）
     * 这里合成一步（ES 7+ 支持创建索引时同时传 settings + mappings）.
     */
    public EsHttpResponse createIndex(boolean recreate) {
        EsHttpResponse head = esRestClient.head("/" + INDEX_NAME);
        if (head.is2xx() && !recreate) {
            return new EsHttpResponse(200, "{\"message\":\"index already exists\"}");
        }
        if (head.is2xx()) {
            esRestClient.delete("/" + INDEX_NAME);
        }

        String mapping;
        try {
            ClassPathResource resource = new ClassPathResource("es/patterns/pattern-index-mapping.json");
            mapping = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new EsHttpResponse(500, "Failed to load mapping: " + e.getMessage());
        }

        return esRestClient.putJson("/" + INDEX_NAME, mapping);
    }

    /** 删除索引 */
    public EsHttpResponse deleteIndex() {
        return esRestClient.delete("/" + INDEX_NAME);
    }

    // ========== 文档 CRUD（对应 coupon-search 的 BaseElasticSearchService） ==========

    /**
     * 索引单个文档.
     *
     * 对应 coupon-search 中的：
     *   baseElasticSearchService.index(INDEX, TYPE, templateId, data)
     *
     * ES 7+ 不再需要 type，直接 PUT /{index}/_doc/{id}
     */
    public EsHttpResponse indexDoc(PatternDoc doc) {
        try {
            String json = om.writeValueAsString(doc);
            String path = "/" + INDEX_NAME + "/_doc/" + doc.getCouponId() + "?refresh=wait_for";
            return esRestClient.putJson(path, json);
        } catch (JsonProcessingException e) {
            return new EsHttpResponse(500, e.getMessage());
        }
    }

    /**
     * 删除单个文档.
     *
     * 对应 coupon-search 中的：
     *   baseElasticSearchService.del(INDEX, TYPE, templateId)
     */
    public EsHttpResponse deleteDoc(Long couponId) {
        return esRestClient.delete("/" + INDEX_NAME + "/_doc/" + couponId + "?refresh=wait_for");
    }

    /**
     * 部分更新文档（只更新指定字段）.
     *
     * 对应 coupon-search 中的：
     *   baseElasticSearchService.updateByDoc(INDEX, TYPE, templateId, fieldMap)
     *
     * 用 POST /{index}/_update/{id}，请求体格式：{"doc": {字段...}}
     * 只会更新传入的字段，其他字段保持不变.
     */
    public EsHttpResponse partialUpdate(Long couponId, Map<String, Object> fields) {
        ObjectNode body = om.createObjectNode();
        body.set("doc", om.valueToTree(fields));
        try {
            String path = "/" + INDEX_NAME + "/_update/" + couponId + "?refresh=wait_for";
            return esRestClient.postJson(path, om.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            return new EsHttpResponse(500, e.getMessage());
        }
    }

    /**
     * 批量索引（Bulk API）.
     *
     * 对应 coupon-search 中 EsBaseOperate.addToEs()
     *
     * Bulk 格式是 NDJSON（每行一个 JSON）：
     *   {"index":{"_index":"xxx","_id":"1"}}
     *   {"couponId":1,"title":"..."}
     *   {"index":{"_index":"xxx","_id":"2"}}
     *   {"couponId":2,"title":"..."}
     *
     * 批量操作在导入大量数据时比逐条 index 快很多.
     */
    public EsHttpResponse bulkIndexSamples() {
        List<PatternDoc> docs = PatternSampleData.samples();
        StringBuilder ndjson = new StringBuilder(2048);
        try {
            for (PatternDoc doc : docs) {
                ObjectNode action = om.createObjectNode();
                action.putObject("index")
                        .put("_index", INDEX_NAME)
                        .put("_id", String.valueOf(doc.getCouponId()));
                ndjson.append(om.writeValueAsString(action)).append('\n');
                ndjson.append(om.writeValueAsString(doc)).append('\n');
            }
        } catch (JsonProcessingException e) {
            return new EsHttpResponse(500, e.getMessage());
        }

        return esRestClient.postNdjson("/_bulk?refresh=true", ndjson.toString());
    }

    // ========== 查询模式执行 ==========

    /**
     * 执行查询并返回：DSL + ES 原始响应 + 解析后的文档列表.
     * 方便对照学习.
     */
    public Map<String, Object> executePattern(ObjectNode dsl) {
        Map<String, Object> result = new LinkedHashMap<>();
        String dslString = patterns.toPrettyJson(dsl);
        result.put("dsl", dsl);

        EsHttpResponse resp = esRestClient.postJson("/" + INDEX_NAME + "/_search", dslString);
        result.put("statusCode", resp.getStatusCode());

        if (resp.is2xx()) {
            try {
                JsonNode root = om.readTree(resp.getBody());
                result.put("took_ms", root.path("took").asInt());

                long total = root.path("hits").path("total").isObject()
                        ? root.path("hits").path("total").path("value").asLong()
                        : root.path("hits").path("total").asLong();
                result.put("total", total);

                JsonNode hits = root.path("hits").path("hits");
                List<Map<String, Object>> docs = new ArrayList<>();
                if (hits.isArray()) {
                    for (JsonNode hit : hits) {
                        Map<String, Object> doc = new LinkedHashMap<>();
                        doc.put("_id", hit.path("_id").asText());
                        doc.put("_score", hit.path("_score").isNull() ? null : hit.path("_score").asDouble());
                        doc.put("_source", om.treeToValue(hit.path("_source"), Map.class));
                        docs.add(doc);
                    }
                }
                result.put("hits", docs);
            } catch (Exception e) {
                result.put("parseError", e.getMessage());
                result.put("rawBody", resp.getBody());
            }
        } else {
            result.put("error", resp.getBody());
        }

        return result;
    }

    public EsQueryPatterns getPatterns() {
        return patterns;
    }
}
