package com.aeon.demo.domain;

/**
 * 券适用范围类型（最小化实现：全场 / 指定SKU）。
 *
 * <p>注意：在 AEON 逻辑中排序是 ConditionType DESC（范围越窄越优先），
 * 因此这里用更大的值代表更窄的范围。</p>
 *
 * @author codex
 */
public enum CouponConditionType {
    ALL(0),
    SKU(1);

    private final int code;

    CouponConditionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

