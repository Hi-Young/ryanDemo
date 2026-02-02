package com.ryan.es.coupon;

import com.ryan.common.base.ResultVO;
import com.ryan.es.http.EsHttpResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/es/coupon")
public class CouponEsController {

    private final CouponEsService couponEsService;

    public CouponEsController(CouponEsService couponEsService) {
        this.couponEsService = couponEsService;
    }

    @GetMapping("/ping")
    public ResultVO<EsHttpResponse> ping() {
        return ResultVO.success(couponEsService.ping());
    }

    @PostMapping("/index")
    public ResultVO<EsHttpResponse> createIndex(@RequestParam(defaultValue = "false") boolean recreate) {
        return ResultVO.success(couponEsService.createCouponIndex(recreate));
    }

    @DeleteMapping("/index")
    public ResultVO<EsHttpResponse> deleteIndex() {
        return ResultVO.success(couponEsService.deleteCouponIndex());
    }

    @PostMapping("/index/refresh")
    public ResultVO<EsHttpResponse> refreshIndex() {
        return ResultVO.success(couponEsService.refreshCouponIndex());
    }

    @PostMapping("/sample/bulk")
    public ResultVO<EsHttpResponse> bulkSample(@RequestParam(defaultValue = "true") boolean refresh) {
        return ResultVO.success(couponEsService.bulkIndexSamples(refresh));
    }

    @PostMapping("/doc")
    public ResultVO<EsHttpResponse> upsert(@RequestBody CouponEsDoc doc,
                                          @RequestParam(defaultValue = "true") boolean refresh) {
        return ResultVO.success(couponEsService.upsert(doc, refresh));
    }

    @GetMapping("/doc/{couponId}")
    public ResultVO<EsHttpResponse> getById(@PathVariable Long couponId) {
        return ResultVO.success(couponEsService.getById(couponId));
    }

    @DeleteMapping("/doc/{couponId}")
    public ResultVO<EsHttpResponse> deleteById(@PathVariable Long couponId) {
        return ResultVO.success(couponEsService.deleteById(couponId));
    }

    /**
     * Helpful for learning: only returns the built DSL (no ES call).
     */
    @PostMapping("/dsl")
    public ResultVO<String> dsl(@RequestBody CouponSearchRequest req) {
        return ResultVO.success(couponEsService.buildDsl(req));
    }

    @PostMapping("/search")
    public ResultVO<CouponSearchResponse> search(@RequestBody CouponSearchRequest req) {
        return ResultVO.success(couponEsService.searchParsed(req));
    }

    @GetMapping("/suggest")
    public ResultVO<List<String>> suggest(@RequestParam String prefix,
                                          @RequestParam(defaultValue = "10") int size) {
        return ResultVO.success(couponEsService.suggestParsed(prefix, size));
    }

    /**
     * Partial update: only update specified fields (AEON-style updateByDoc).
     */
    @PatchMapping("/doc/{couponId}")
    public ResultVO<EsHttpResponse> partialUpdate(@PathVariable Long couponId,
                                                  @RequestBody Map<String, Object> fields,
                                                  @RequestParam(defaultValue = "true") boolean refresh) {
        return ResultVO.success(couponEsService.partialUpdate(couponId, fields, refresh));
    }

    /**
     * Preview advanced search DSL without executing (for learning).
     */
    @PostMapping("/advanced/dsl")
    public ResultVO<String> advancedDsl(@RequestBody CouponSearchRequest req) {
        return ResultVO.success(couponEsService.buildAdvancedDsl(req));
    }

    /**
     * Execute advanced search using ChineseSearchStrategy + nested query.
     */
    @PostMapping("/advanced/search")
    public ResultVO<CouponSearchResponse> advancedSearch(@RequestBody CouponSearchRequest req) {
        return ResultVO.success(couponEsService.advancedSearch(req));
    }
}

