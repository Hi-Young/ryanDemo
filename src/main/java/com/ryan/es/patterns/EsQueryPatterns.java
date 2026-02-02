package com.ryan.es.patterns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

/**
 * ES 查询模式教学类 —— 从 coupon-search 项目提炼的 7 种核心模式.
 *
 * <pre>
 * 每个 buildXxx 方法都是一个独立的教学单元：
 *   1. BoolQuery 骨架         — 所有查询的基础结构
 *   2. Term / Terms 精确匹配  — filter 中最常用
 *   3. Range 范围查询         — 日期、金额过滤
 *   4. Nested 嵌套查询        — 查询嵌套文档（门店）
 *   5. DisMax 最佳匹配        — 多策略搜索取最高分
 *   6. 排序 + 分页            — 实际业务必备
 *   7. 综合示例               — 把上面全部组合在一起（= coupon-search 的真实写法）
 * </pre>
 *
 * <b>设计说明</b>：这里用 Jackson ObjectNode 手动拼 JSON DSL，
 * 和 coupon-search 用 SearchSourceBuilder + QueryBuilders 的效果完全等价。
 * 区别只在于 coupon-search 依赖 ES 官方 Java 库来构建 JSON，
 * 而这里直接拼 JSON 让你看到最终发给 ES 的 DSL 长什么样。
 */
public class EsQueryPatterns {

    private final ObjectMapper om;

    public EsQueryPatterns() {
        this.om = new ObjectMapper();
    }

    /** 带缩进的 JSON 输出（方便查看 DSL） */
    public String toPrettyJson(ObjectNode node) {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    // ====================================================================
    //  模式 1：BoolQuery 骨架
    // ====================================================================
    //
    //  这是 coupon-search 中 *所有* 搜索的基础结构。
    //  一个 bool 查询有 4 个槽位：
    //    must     → 必须匹配，参与评分（用于关键词搜索）
    //    filter   → 必须匹配，不参与评分（用于精确过滤，性能更好）
    //    should   → 可选匹配，匹配了加分
    //    must_not → 必须不匹配
    //
    //  coupon-search 的核心思路：
    //    - 关键词全文搜索 → 放 must（因为需要 ES 计算相关性分数）
    //    - 分类、门店、日期等过滤 → 放 filter（只需要 yes/no，不需要打分）
    //
    //  生成的 DSL 等价于 coupon-search 中的：
    //    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    //    boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword));
    //    boolQueryBuilder.filter(QueryBuilders.termQuery("couponCategory", 1));
    //
    // ====================================================================

    /**
     * 模式 1：BoolQuery 骨架.
     *
     * @param keyword        搜索关键词（放 must，参与评分）
     * @param couponCategory 分类过滤（放 filter，不评分）
     */
    public ObjectNode buildBoolQuerySkeleton(String keyword, Integer couponCategory) {
        ObjectNode root = om.createObjectNode();
        ObjectNode bool = root.putObject("query").putObject("bool");

        // ---- must: 关键词全文搜索（参与评分） ----
        ArrayNode must = bool.putArray("must");
        if (StringUtils.hasText(keyword)) {
            // match 查询：会对 keyword 分词后去匹配
            // operator=and 表示所有分出来的词都必须命中
            ObjectNode matchNode = must.addObject().putObject("match").putObject("title");
            matchNode.put("query", keyword);
            matchNode.put("operator", "and");
        } else {
            // 没有关键词时匹配全部文档
            must.addObject().putObject("match_all");
        }

        // ---- filter: 精确过滤（不评分，有缓存加速） ----
        ArrayNode filter = bool.putArray("filter");
        if (couponCategory != null) {
            // term 查询：精确匹配，不分词
            filter.addObject().putObject("term").put("couponCategory", couponCategory);
        }

        return root;
    }

    // ====================================================================
    //  模式 2：Term / Terms 精确匹配
    // ====================================================================
    //
    //  term  → 精确匹配单个值（如 couponCategory = 1）
    //  terms → 精确匹配多个值中的任意一个（如 status IN ("active","pending")）
    //
    //  coupon-search 中大量使用：
    //    boolQueryBuilder.filter(QueryBuilders.termQuery("couponCategory", 1));
    //
    //  注意：term 不分词，所以只能用在 keyword / integer / long 等类型字段上
    //  如果对 text 类型字段用 term，几乎一定查不到（因为 text 字段存储的是分词后的词条）
    //
    // ====================================================================

    /**
     * 模式 2：Term + Terms 过滤.
     *
     * @param status   状态精确匹配（单值）
     * @param cityCode 城市编码精确匹配（单值）
     */
    public ObjectNode buildTermFilters(String status, String cityCode) {
        ObjectNode root = om.createObjectNode();
        ObjectNode bool = root.putObject("query").putObject("bool");
        bool.putArray("must").addObject().putObject("match_all");
        ArrayNode filter = bool.putArray("filter");

        // term: 精确匹配单个值
        if (StringUtils.hasText(status)) {
            filter.addObject().putObject("term").put("status", status);
        }

        if (StringUtils.hasText(cityCode)) {
            filter.addObject().putObject("term").put("cityCode", cityCode);
        }

        // terms: 匹配多个值中的任意一个（等价于 SQL 的 IN）
        // 示例：couponCategory IN (1, 2)
        ArrayNode categories = filter.addObject().putObject("terms").putArray("couponCategory");
        categories.add(1);
        categories.add(2);

        return root;
    }

    // ====================================================================
    //  模式 3：Range 范围查询
    // ====================================================================
    //
    //  coupon-search 中用 range 查询实现：
    //    - 日期范围过滤（券在有效期内）
    //    - 金额范围过滤（优惠金额在某个区间）
    //
    //  操作符：gte（>=）, gt（>）, lte（<=）, lt（<）
    //
    //  对应 coupon-search 中的：
    //    boolQueryBuilder.filter(QueryBuilders.rangeQuery("useStartTime").gte(startTime));
    //    boolQueryBuilder.filter(QueryBuilders.rangeQuery("useEndTime").lte(endTime));
    //
    // ====================================================================

    /**
     * 模式 3：Range 范围查询.
     *
     * @param minDiscount 最小优惠金额
     * @param maxDiscount 最大优惠金额
     * @param validNow    是否只查当前有效的券
     */
    public ObjectNode buildRangeFilters(Double minDiscount, Double maxDiscount, boolean validNow) {
        ObjectNode root = om.createObjectNode();
        ObjectNode bool = root.putObject("query").putObject("bool");
        bool.putArray("must").addObject().putObject("match_all");
        ArrayNode filter = bool.putArray("filter");

        // 金额范围
        if (minDiscount != null || maxDiscount != null) {
            ObjectNode range = filter.addObject().putObject("range").putObject("discountAmount");
            if (minDiscount != null) {
                range.put("gte", minDiscount);   // >= 最小金额
            }
            if (maxDiscount != null) {
                range.put("lte", maxDiscount);   // <= 最大金额
            }
        }

        // 日期范围：只查当前有效的券（startTime <= now AND endTime >= now）
        // ES 支持 "now" 关键字，表示当前时间
        if (validNow) {
            filter.addObject()
                    .putObject("range")
                    .putObject("startTime")
                    .put("lte", "now");       // 已经开始

            filter.addObject()
                    .putObject("range")
                    .putObject("endTime")
                    .put("gte", "now");       // 还没结束
        }

        return root;
    }

    // ====================================================================
    //  模式 4：Nested 嵌套查询
    // ====================================================================
    //
    //  coupon-search 中，一张券可以在多个门店使用，
    //  门店信息以 nested 类型存储（stores 数组内的每个对象是独立的 Lucene 文档）。
    //
    //  为什么要用 nested 而不是普通 object？
    //  假设一张券有两个门店：[{code:"S001", name:"北京店"}, {code:"S002", name:"上海店"}]
    //  如果用普通 object，ES 会把数组"扁平化"：code=["S001","S002"], name=["北京店","上海店"]
    //  查询 "code=S001 AND name=上海店" 会错误命中（因为 S001 和 上海店 来自不同门店）
    //  用 nested 就不会有这个问题——每个门店对象的字段保持绑定关系。
    //
    //  对应 coupon-search 中的：
    //    boolQueryBuilder.filter(
    //        QueryBuilders.nestedQuery("stores",
    //            QueryBuilders.termQuery("stores.code", storeCode),
    //            ScoreMode.None));
    //
    // ====================================================================

    /**
     * 模式 4：Nested 嵌套查询.
     *
     * @param storeCode       按门店编码过滤
     * @param corporationCode 按法人编码过滤
     */
    public ObjectNode buildNestedQuery(String storeCode, String corporationCode) {
        ObjectNode root = om.createObjectNode();
        ObjectNode bool = root.putObject("query").putObject("bool");
        bool.putArray("must").addObject().putObject("match_all");
        ArrayNode filter = bool.putArray("filter");

        // nested 查询的三个必填项：
        //   path  → 嵌套字段的路径（"stores"）
        //   query → 对嵌套对象内部字段的查询
        //   score_mode → 评分合并策略（none=不评分，只做过滤）

        if (StringUtils.hasText(storeCode)) {
            ObjectNode nested = filter.addObject().putObject("nested");
            nested.put("path", "stores");
            nested.put("score_mode", "none");
            // 注意：嵌套查询内部的字段名要带路径前缀 "stores.xxx"
            nested.putObject("query")
                    .putObject("term")
                    .put("stores.code", storeCode);
        }

        if (StringUtils.hasText(corporationCode)) {
            ObjectNode nested = filter.addObject().putObject("nested");
            nested.put("path", "stores");
            nested.put("score_mode", "none");
            nested.putObject("query")
                    .putObject("term")
                    .put("stores.corporationCode", corporationCode);
        }

        return root;
    }

    // ====================================================================
    //  模式 5：DisMax 最佳匹配（中文搜索核心策略）
    // ====================================================================
    //
    //  这是 coupon-search 中最复杂也最核心的部分。
    //
    //  问题：用户搜索 "满减" 时，应该用哪种查询方式？
    //    - prefix（前缀匹配）：能匹配 "满减优惠券"
    //    - match（分词匹配）：能匹配 "满100减50"
    //    - match_phrase（短语匹配）：能匹配标题中连续出现 "满减" 的
    //
    //  答案是：全都用！然后取得分最高的那个。这就是 dis_max 的作用。
    //
    //  dis_max 会执行所有子查询，但最终得分 = 最高的那个子查询的得分
    //  （而不是把所有子查询的分加起来），这样可以避免多个低质量匹配的分数叠加
    //  超过一个高质量匹配的情况。
    //
    //  coupon-search 还会根据关键词长度和类型（纯中文/纯英文/混合）
    //  动态调整子查询的组合和 boost 权重：
    //    - 1 个字 → prefix + match
    //    - 2-4 个字 → prefix + smart_word + standard
    //    - 5+ 个字 → prefix + phrase + smart_word + max_word + standard
    //
    //  对应 coupon-search 中 BaseChineseQuery 接口的 buildOnlyChineseQuery 等方法。
    //
    // ====================================================================

    /**
     * 模式 5：DisMax 最佳匹配.
     * <p>
     * 这里演示的是不依赖 IK 分词器的版本（使用 standard analyzer），
     * 核心思路和 coupon-search 完全相同。
     *
     * @param keyword 搜索关键词
     */
    public ObjectNode buildDisMaxQuery(String keyword) {
        ObjectNode root = om.createObjectNode();

        if (!StringUtils.hasText(keyword)) {
            root.putObject("query").putObject("match_all");
            return root;
        }

        String trimmed = keyword.trim();
        int len = trimmed.length();

        // dis_max: 多个子查询取最高分
        ObjectNode disMax = root.putObject("query").putObject("dis_max");
        // tie_breaker: 非最高分的子查询贡献多少（0 = 完全不贡献，1 = 完全贡献）
        // 0.3 表示非最佳匹配的子查询贡献 30% 的分数
        disMax.put("tie_breaker", 0.3);
        ArrayNode queries = disMax.putArray("queries");

        // 策略1: prefix 前缀匹配（权重最高）
        // 用户输入 "满减" → 匹配所有以 "满减" 开头的标题
        // 用 lowercase 子字段，实现大小写不敏感的前缀匹配
        ObjectNode prefixQ = queries.addObject().putObject("prefix").putObject("title.lowercase");
        prefixQ.put("value", trimmed.toLowerCase());
        prefixQ.put("boost", 5.0);

        if (len <= 4) {
            // 短关键词策略：match 查询
            ObjectNode matchQ = queries.addObject().putObject("match").putObject("title");
            matchQ.put("query", trimmed);
            matchQ.put("operator", "and");
            matchQ.put("boost", 2.0);

            // 如果够长（>=4字），加模糊匹配容错
            if (len >= 4) {
                ObjectNode fuzzyQ = queries.addObject().putObject("match").putObject("title");
                fuzzyQ.put("query", trimmed);
                fuzzyQ.put("fuzziness", 1);            // 允许 1 个字符的编辑距离
                fuzzyQ.put("prefix_length", 2);         // 前 2 个字符必须精确匹配
                fuzzyQ.put("max_expansions", 10);       // 最多扩展 10 个变体
                fuzzyQ.put("boost", 0.8);
            }
        } else {
            // 长关键词策略（5+ 字）

            // match_phrase: 短语匹配（要求词序一致）
            // slop=2 表示允许中间间隔最多 2 个词
            ObjectNode phraseQ = queries.addObject().putObject("match_phrase").putObject("title");
            phraseQ.put("query", trimmed);
            phraseQ.put("slop", 2);
            phraseQ.put("boost", 4.0);

            // match: 分词匹配 + minimum_should_match
            // 80% 表示分出来的词至少要命中 80%
            ObjectNode matchQ = queries.addObject().putObject("match").putObject("title");
            matchQ.put("query", trimmed);
            matchQ.put("operator", "and");
            matchQ.put("boost", 2.0);

            // fuzzy: 容错匹配
            ObjectNode fuzzyQ = queries.addObject().putObject("match").putObject("title");
            fuzzyQ.put("query", trimmed);
            fuzzyQ.put("fuzziness", len >= 7 ? 2 : 1); // 越长容错越大
            fuzzyQ.put("prefix_length", 2);
            fuzzyQ.put("max_expansions", 10);
            fuzzyQ.put("boost", 0.5);
        }

        return root;
    }

    // ====================================================================
    //  模式 6：排序 + 分页
    // ====================================================================
    //
    //  coupon-search 支持多种排序方式，通过 sort 参数切换：
    //    - 默认 → 按相关性分数降序
    //    - 优惠金额 → 按 discountAmount 排序
    //    - 领取量 → 按 receiveCount 降序
    //
    //  分页用 from + size：
    //    from = (pageNo - 1) * pageSize
    //    size = pageSize
    //
    //  对应 coupon-search 中的：
    //    searchSourceBuilder.from(pageSize * (pageIndex - 1));
    //    searchSourceBuilder.size(pageSize);
    //    searchSourceBuilder.sort("parValue", SortOrder.ASC);
    //
    // ====================================================================

    /**
     * 模式 6：排序 + 分页.
     *
     * @param sortType "score" / "discount_asc" / "discount_desc" / "receive_desc"
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     */
    public ObjectNode buildSortAndPaging(String sortType, int pageNo, int pageSize) {
        ObjectNode root = om.createObjectNode();

        // 分页
        int from = Math.max(0, (pageNo - 1) * pageSize);
        root.put("from", from);
        root.put("size", Math.min(pageSize, 100));  // 限制最大 100 防止拉爆

        // 匹配所有（排序示例，不做筛选）
        root.putObject("query").putObject("match_all");

        // 排序（ES 按数组顺序依次排序）
        ArrayNode sort = root.putArray("sort");

        String type = sortType == null ? "score" : sortType.toLowerCase();
        switch (type) {
            case "discount_asc":
                // 按优惠金额升序（小 → 大）
                sort.addObject().putObject("discountAmount").put("order", "asc");
                // 二级排序：金额相同时按相关性
                sort.addObject().putObject("_score").put("order", "desc");
                break;

            case "discount_desc":
                // 按优惠金额降序（大 → 小）
                sort.addObject().putObject("discountAmount").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;

            case "receive_desc":
                // 按领取量降序
                sort.addObject().putObject("receiveCount").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;

            case "score":
            default:
                // 默认按相关性分数降序
                sort.addObject().putObject("_score").put("order", "desc");
                break;
        }

        return root;
    }

    // ====================================================================
    //  模式 7：综合示例 —— 模拟 coupon-search 的完整搜索流程
    // ====================================================================
    //
    //  把上面所有模式组合在一起，这就是 coupon-search 中
    //  SearchCouponEsServiceImpl.search() 方法的完整逻辑。
    //
    //  一个真实请求的构建过程：
    //    1. 创建 BoolQuery 骨架
    //    2. 有关键词 → must 里放 DisMax 多策略搜索
    //    3. 有分类过滤 → filter 里放 term
    //    4. 有门店过滤 → filter 里放 nested query
    //    5. 有日期过滤 → filter 里放 range
    //    6. 设置排序
    //    7. 设置分页
    //
    // ====================================================================

    /**
     * 模式 7：综合示例.
     *
     * @param keyword         搜索关键词
     * @param couponCategory  分类过滤
     * @param storeCode       门店过滤
     * @param corporationCode 法人过滤
     * @param validNow        是否只查有效券
     * @param sortType        排序方式
     * @param pageNo          页码
     * @param pageSize        每页大小
     */
    public ObjectNode buildFullSearch(String keyword,
                                      Integer couponCategory,
                                      String storeCode,
                                      String corporationCode,
                                      boolean validNow,
                                      String sortType,
                                      int pageNo,
                                      int pageSize) {
        ObjectNode root = om.createObjectNode();

        // ---- 分页 ----
        int from = Math.max(0, (pageNo - 1) * pageSize);
        root.put("from", from);
        root.put("size", Math.min(pageSize, 100));

        // ---- 限制返回字段（减小网络传输） ----
        ArrayNode source = root.putArray("_source");
        source.add("couponId");
        source.add("title");
        source.add("couponCategory");
        source.add("discountAmount");
        source.add("receiveCount");
        source.add("startTime");
        source.add("endTime");

        // ---- 构建 BoolQuery ----
        ObjectNode bool = om.createObjectNode();
        ArrayNode must = bool.putArray("must");
        ArrayNode filter = bool.putArray("filter");

        // Step 1: 关键词搜索 → must（DisMax 多策略）
        if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            int len = trimmed.length();

            ObjectNode disMax = must.addObject().putObject("dis_max");
            disMax.put("tie_breaker", 0.3);
            ArrayNode queries = disMax.putArray("queries");

            // prefix
            ObjectNode prefixQ = queries.addObject().putObject("prefix").putObject("title.lowercase");
            prefixQ.put("value", trimmed.toLowerCase());
            prefixQ.put("boost", 5.0);

            // match
            ObjectNode matchQ = queries.addObject().putObject("match").putObject("title");
            matchQ.put("query", trimmed);
            matchQ.put("operator", "and");
            matchQ.put("boost", 2.0);

            // match_phrase（仅长关键词）
            if (len >= 5) {
                ObjectNode phraseQ = queries.addObject().putObject("match_phrase").putObject("title");
                phraseQ.put("query", trimmed);
                phraseQ.put("slop", 2);
                phraseQ.put("boost", 4.0);
            }
        } else {
            must.addObject().putObject("match_all");
        }

        // Step 2: 分类过滤 → filter（term）
        if (couponCategory != null) {
            filter.addObject().putObject("term").put("couponCategory", couponCategory);
        }

        // Step 3: 门店过滤 → filter（nested）
        if (StringUtils.hasText(storeCode)) {
            ObjectNode nested = filter.addObject().putObject("nested");
            nested.put("path", "stores");
            nested.put("score_mode", "none");
            nested.putObject("query")
                    .putObject("term")
                    .put("stores.code", storeCode);
        }

        // Step 4: 法人过滤 → filter（nested）
        if (StringUtils.hasText(corporationCode)) {
            ObjectNode nested = filter.addObject().putObject("nested");
            nested.put("path", "stores");
            nested.put("score_mode", "none");
            nested.putObject("query")
                    .putObject("term")
                    .put("stores.corporationCode", corporationCode);
        }

        // Step 5: 有效期过滤 → filter（range）
        if (validNow) {
            filter.addObject().putObject("range").putObject("startTime").put("lte", "now");
            filter.addObject().putObject("range").putObject("endTime").put("gte", "now");
        }

        // 把 bool 挂到 query 上
        root.putObject("query").set("bool", bool);

        // ---- 排序 ----
        ArrayNode sort = root.putArray("sort");
        String type = sortType == null ? "score" : sortType.toLowerCase();
        switch (type) {
            case "discount_asc":
                sort.addObject().putObject("discountAmount").put("order", "asc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "discount_desc":
                sort.addObject().putObject("discountAmount").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            case "receive_desc":
                sort.addObject().putObject("receiveCount").put("order", "desc");
                sort.addObject().putObject("_score").put("order", "desc");
                break;
            default:
                sort.addObject().putObject("_score").put("order", "desc");
                break;
        }

        return root;
    }
}
