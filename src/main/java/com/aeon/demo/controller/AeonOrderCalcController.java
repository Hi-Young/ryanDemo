package com.aeon.demo.controller;

import com.aeon.demo.dto.AeonOrderCalcRequest;
import com.aeon.demo.dto.AeonOrderCalcResponse;
import com.aeon.demo.dto.CartItemRequest;
import com.aeon.demo.scenario.AeonScenarioFactory;
import com.aeon.demo.service.AeonOrderCalcService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * AEON(永旺)「促销+券」演示接口。
 *
 * @author codex
 */
@RestController
@Profile("aeon-demo")
@RequestMapping("/aeon-demo")
public class AeonOrderCalcController {

    private final AeonOrderCalcService orderCalcService;

    public AeonOrderCalcController(AeonOrderCalcService orderCalcService) {
        this.orderCalcService = orderCalcService;
    }

    /**
     * 订单确认计算：先促销，后用券；用券后运费二次计算，运费券重算。
     */
    @PostMapping("/order/calc")
    public AeonOrderCalcResponse calc(@RequestBody AeonOrderCalcRequest request) {
        return orderCalcService.calc(request);
    }

    /**
     * 返回一个可直接复制到 Postman 的示例请求。
     */
    @GetMapping("/order/sample")
    public AeonOrderCalcRequest sample(@RequestParam(value = "scenario", required = false) String scenario) {
        AeonOrderCalcRequest req = new AeonOrderCalcRequest();
        req.setScenario(scenario == null ? AeonScenarioFactory.SCENARIO_S1 : scenario);
        req.setMemberId("M10001");
        req.setStoreCode("S001");
        req.setChannel(1);
        req.setPlatform("APP");
        req.setLogisticFee(null); // 为空表示按场景运费规则自动计算

        CartItemRequest apple = new CartItemRequest();
        apple.setCartItemId("C1");
        apple.setSkuId("SKU-APPLE");
        apple.setQuantity(2);
        apple.setSalePrice(new BigDecimal("30.00"));

        CartItemRequest beef = new CartItemRequest();
        beef.setCartItemId("C2");
        beef.setSkuId("SKU-BEEF");
        beef.setQuantity(1);
        beef.setSalePrice(new BigDecimal("120.00"));

        CartItemRequest milk = new CartItemRequest();
        milk.setCartItemId("C3");
        milk.setSkuId("SKU-MILK");
        milk.setQuantity(3);
        milk.setSalePrice(new BigDecimal("20.00"));

        req.setCartItems(Arrays.asList(apple, beef, milk));
        // coupons 为空表示使用场景内置券（S1 内置了商品券+运费券+互斥组）
        return req;
    }
}

