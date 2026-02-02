package com.bruce.promotiondemo.model;

/**
 * 规则类型
 */
public enum RuleType {
    /**
     * 折扣（如8折）
     */
    DISCOUNT,

    /**
     * 直减（如减20元）
     */
    AMOUNT_OFF,

    /**
     * 满减（如满200减20）
     */
    THRESHOLD_AMOUNT_OFF
}
