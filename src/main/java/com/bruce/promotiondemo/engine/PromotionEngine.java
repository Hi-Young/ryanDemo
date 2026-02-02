package com.bruce.promotiondemo.engine;

import com.bruce.promotiondemo.model.Cart;
import com.bruce.promotiondemo.model.Rule;

import java.util.List;

/**
 * 促销计算引擎接口
 */
public interface PromotionEngine {

    /**
     * 计算促销
     *
     * @param cart  购物车
     * @param rules 规则列表
     * @return 计算后的购物车
     */
    Cart calculate(Cart cart, List<Rule> rules);

    /**
     * 获取引擎名称
     */
    String getName();
}
