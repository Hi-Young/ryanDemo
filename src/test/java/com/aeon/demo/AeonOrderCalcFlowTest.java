package com.aeon.demo;

import com.aeon.demo.dto.AeonOrderCalcRequest;
import com.aeon.demo.dto.AeonOrderCalcResponse;
import com.aeon.demo.dto.CartItemRequest;
import com.aeon.demo.scenario.AeonScenarioFactory;
import com.aeon.demo.service.AeonOrderCalcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 核心流程单测：验证「先促销后用券 + 运费二次计算 + 运费券重算」能跑通且结果符合预期。
 *
 * @author codex
 */
@ActiveProfiles("aeon-demo")
@SpringBootTest(classes = com.aeon.demo.AeonDemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.config.name=aeon-demo"})
public class AeonOrderCalcFlowTest {

    @Autowired
    private AeonOrderCalcService orderCalcService;

    @Test
    void should_calc_like_aeon_flow_s1() {
        AeonOrderCalcRequest req = new AeonOrderCalcRequest();
        req.setScenario(AeonScenarioFactory.SCENARIO_S1);
        req.setMemberId("M10001");
        req.setStoreCode("S001");
        req.setChannel(1);
        req.setPlatform("APP");

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

        AeonOrderCalcResponse resp = orderCalcService.calc(req);
        assertNotNull(resp);

        // 1) 原价：60 + 120 + 60 = 240
        assertEquals(0, resp.getPromo().getOriginalGoodsAmount().compareTo(new BigDecimal("240.00")));

        // 5) 促销后：苹果直降 + 牛肉牛奶满减 = 200
        assertEquals(0, resp.getPromo().getPromoGoodsAmount().compareTo(new BigDecimal("200.00")));

        // 8) 商品券：选中 70 + 40 + 35 = 145，商品实付=55
        assertEquals(0, resp.getAmountSummary().getGoodsCouponDiscount().compareTo(new BigDecimal("145.00")));
        assertEquals(0, resp.getAmountSummary().getGoodsPayAmount().compareTo(new BigDecimal("55.00")));

        // 运费：用券前(200>=199免运费)=0；用券后(55<199)=12
        assertEquals(0, resp.getFreight().getFreightBeforeCoupon().compareTo(new BigDecimal("0.00")));
        assertEquals(0, resp.getFreight().getFreightAfterGoodsCoupon().compareTo(new BigDecimal("12.00")));

        // 运费券重算：选中 6 + 5 = 11，运费实付=1，最终应付=56
        assertEquals(0, resp.getAmountSummary().getShippingCouponDiscount().compareTo(new BigDecimal("11.00")));
        assertEquals(0, resp.getAmountSummary().getFreightPayAmount().compareTo(new BigDecimal("1.00")));
        assertEquals(0, resp.getAmountSummary().getFinalPayAmount().compareTo(new BigDecimal("56.00")));

        String checkedGoods = resp.getGoodsCoupons().getAvailableCouponList().stream()
                .filter(c -> c.isChecked())
                .map(c -> c.getCouponNo())
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("G202-1,G203-1,G205-1", checkedGoods);

        String checkedShipping = resp.getShippingCouponsAfterFreightRecalc().getAvailableCouponList().stream()
                .filter(c -> c.isChecked())
                .map(c -> c.getCouponNo())
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("S302-1,S303-1", checkedShipping);
    }
}
