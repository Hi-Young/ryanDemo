package com.ryan.es.patterns;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ryan.common.base.ResultVO;
import com.ryan.es.http.EsHttpResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ES 查询模式教学 API.
 *
 * <pre>
 * 使用方式：
 *   1. POST /api/es/pattern/setup           → 创建索引 + 导入样本数据
 *   2. POST /api/es/pattern/1-bool          → 查看模式1的 DSL 和执行结果
 *   3. POST /api/es/pattern/2-term          → 查看模式2
 *   4. ...依次测试每个模式
 *   5. POST /api/es/pattern/7-full          → 综合查询
 *   6. DELETE /api/es/pattern/cleanup        → 清理索引
 *
 * 每个接口都会同时返回：
 *   - dsl：发给 ES 的完整 JSON 查询（可以复制到 Kibana 执行）
 *   - total：命中总数
 *   - hits：命中的文档列表
 * </pre>
 */
@RestController
@RequestMapping("/api/es/pattern")
public class EsPatternController {

    private final EsPatternService service;

    public EsPatternController(EsPatternService service) {
        this.service = service;
    }

    // ==================== 环境准备 ====================

    /** 一键初始化：创建索引 + 导入 6 条样本数据 */
    @PostMapping("/setup")
    public ResultVO<String> setup() {
        EsHttpResponse createResp = service.createIndex(true);
        if (!createResp.is2xx()) {
            return ResultVO.error("创建索引失败: " + createResp.getBody());
        }

        EsHttpResponse bulkResp = service.bulkIndexSamples();
        if (!bulkResp.is2xx()) {
            return ResultVO.error("导入数据失败: " + bulkResp.getBody());
        }

        return ResultVO.success("索引创建成功，已导入 6 条样本数据，可以开始测试各模式");
    }

    /** 清理索引 */
    @DeleteMapping("/cleanup")
    public ResultVO<EsHttpResponse> cleanup() {
        return ResultVO.success(service.deleteIndex());
    }

    // ==================== 模式 1：BoolQuery 骨架 ====================

    /**
     * 模式 1：BoolQuery = must（评分搜索） + filter（精确过滤）.
     *
     * 试试这些参数组合，观察 DSL 和结果的变化：
     *   - keyword=满减                        → must 中有 match，命中包含"满减"的券
     *   - keyword=满减 & couponCategory=1      → 加了 filter，只返回满减券分类
     *   - couponCategory=2                    → 没有关键词时 must 变成 match_all
     */
    @PostMapping("/1-bool")
    public ResultVO<Map<String, Object>> pattern1Bool(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer couponCategory) {

        ObjectNode dsl = service.getPatterns().buildBoolQuerySkeleton(keyword, couponCategory);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 模式 2：Term / Terms 精确匹配 ====================

    /**
     * 模式 2：Term（单值精确）+ Terms（多值 IN）.
     *
     * 试试：
     *   - status=active               → 只返回有效的券
     *   - status=expired              → 只返回过期的券
     *   - cityCode=110000             → 只返回北京的券
     *   - status=active & cityCode=310000 → 上海的有效券
     */
    @PostMapping("/2-term")
    public ResultVO<Map<String, Object>> pattern2Term(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cityCode) {

        ObjectNode dsl = service.getPatterns().buildTermFilters(status, cityCode);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 模式 3：Range 范围查询 ====================

    /**
     * 模式 3：Range 范围过滤（日期、金额）.
     *
     * 试试：
     *   - minDiscount=15               → 优惠金额 >= 15 的券
     *   - minDiscount=10 & maxDiscount=30 → 优惠金额在 10~30 之间
     *   - validNow=true                → 只返回当前在有效期内的券
     *   - validNow=true & minDiscount=20 → 当前有效且优惠 >= 20
     */
    @PostMapping("/3-range")
    public ResultVO<Map<String, Object>> pattern3Range(
            @RequestParam(required = false) Double minDiscount,
            @RequestParam(required = false) Double maxDiscount,
            @RequestParam(defaultValue = "false") boolean validNow) {

        ObjectNode dsl = service.getPatterns().buildRangeFilters(minDiscount, maxDiscount, validNow);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 模式 4：Nested 嵌套查询 ====================

    /**
     * 模式 4：Nested 嵌套查询（门店过滤）.
     *
     * 试试：
     *   - storeCode=S001              → 在"北京朝阳店"可用的券（有3张）
     *   - storeCode=S003              → 在"上海浦东店"可用的券
     *   - corporationCode=CORP_BJ     → 北京法人下所有门店可用的券
     *   - storeCode=S005 & corporationCode=CORP_GZ → 广州天河店 + 广州法人
     */
    @PostMapping("/4-nested")
    public ResultVO<Map<String, Object>> pattern4Nested(
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) String corporationCode) {

        ObjectNode dsl = service.getPatterns().buildNestedQuery(storeCode, corporationCode);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 模式 5：DisMax 最佳匹配 ====================

    /**
     * 模式 5：DisMax 多策略搜索.
     *
     * 试试不同长度的关键词，观察 DSL 子查询的变化：
     *   - keyword=满          → 1字：prefix + match（2个子查询）
     *   - keyword=满减        → 2字：prefix + match（2个子查询）
     *   - keyword=优惠券      → 3字：prefix + match（2个子查询）
     *   - keyword=满减优惠券  → 5字：prefix + match_phrase + match + fuzzy（4个子查询）
     *   - keyword=新人专享满减优惠 → 7字：prefix + phrase + match + fuzzy(fuzziness=2)
     */
    @PostMapping("/5-dismax")
    public ResultVO<Map<String, Object>> pattern5DisMax(
            @RequestParam(required = false) String keyword) {

        ObjectNode dsl = service.getPatterns().buildDisMaxQuery(keyword);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 模式 6：排序 + 分页 ====================

    /**
     * 模式 6：排序 + 分页.
     *
     * 试试：
     *   - sort=score                    → 默认按相关性
     *   - sort=discount_desc            → 优惠金额从高到低
     *   - sort=discount_asc             → 优惠金额从低到高
     *   - sort=receive_desc             → 领取量从高到低
     *   - sort=receive_desc & pageNo=1 & pageSize=2 → 每页2条，第1页
     *   - sort=receive_desc & pageNo=2 & pageSize=2 → 每页2条，第2页
     */
    @PostMapping("/6-sort")
    public ResultVO<Map<String, Object>> pattern6Sort(
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        ObjectNode dsl = service.getPatterns().buildSortAndPaging(sort, pageNo, pageSize);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 模式 7：综合示例 ====================

    /**
     * 模式 7：综合查询 —— coupon-search 的真实写法.
     *
     * 试试一个完整的业务场景：
     *   - keyword=满减 & storeCode=S001 & validNow=true
     *     → 搜索北京朝阳店里当前有效的满减相关优惠券
     *
     *   - keyword=优惠 & couponCategory=1 & sort=discount_desc & pageSize=2
     *     → 搜索满减类别里包含"优惠"的券，按优惠金额降序，每页2条
     *
     *   - corporationCode=CORP_SH & validNow=true & sort=receive_desc
     *     → 上海法人下所有当前有效的券，按领取量排序
     */
    @PostMapping("/7-full")
    public ResultVO<Map<String, Object>> pattern7Full(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer couponCategory,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) String corporationCode,
            @RequestParam(defaultValue = "false") boolean validNow,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        ObjectNode dsl = service.getPatterns().buildFullSearch(
                keyword, couponCategory, storeCode, corporationCode,
                validNow, sort, pageNo, pageSize);
        return ResultVO.success(service.executePattern(dsl));
    }

    // ==================== 文档 CRUD 操作演示 ====================

    /**
     * 索引（新增/覆盖）单个文档.
     *
     * 对应 coupon-search 中通过 MQ 接收消息后调用
     * baseElasticSearchService.index() 写入 ES.
     */
    @PostMapping("/doc")
    public ResultVO<EsHttpResponse> indexDoc(@RequestBody PatternDoc doc) {
        return ResultVO.success(service.indexDoc(doc));
    }

    /** 删除文档 */
    @DeleteMapping("/doc/{couponId}")
    public ResultVO<EsHttpResponse> deleteDoc(@PathVariable Long couponId) {
        return ResultVO.success(service.deleteDoc(couponId));
    }

    /**
     * 部分更新（只更新传入的字段）.
     *
     * 对应 coupon-search 中
     * baseElasticSearchService.updateByDoc(INDEX, TYPE, id, fieldMap)
     *
     * 例如只更新领取数量：{"receiveCount": 9999}
     */
    @PatchMapping("/doc/{couponId}")
    public ResultVO<EsHttpResponse> partialUpdate(@PathVariable Long couponId,
                                                   @RequestBody Map<String, Object> fields) {
        return ResultVO.success(service.partialUpdate(couponId, fields));
    }
}
