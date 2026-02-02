package com.bruce.promotiondemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 促销规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    /**
     * 规则ID
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则类型
     */
    private RuleType type;

    /**
     * 门槛金额（满减时使用）
     */
    private BigDecimal threshold;

    /**
     * 优惠值：折扣类型为折扣率（如0.8表示8折），减免类型为减免金额
     */
    private BigDecimal discount;

    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 互斥规则ID集合
     */
    @Builder.Default
    private Set<String> exclusiveRuleIds = new HashSet<>();

    /**
     * 判断是否与另一个规则互斥
     */
    public boolean isExclusiveWith(Rule other) {
        return exclusiveRuleIds.contains(other.getId())
                || other.getExclusiveRuleIds().contains(this.getId());
    }

    /**
     * 深拷贝
     */
    public Rule copy() {
        return Rule.builder()
                .id(this.id)
                .name(this.name)
                .type(this.type)
                .threshold(this.threshold)
                .discount(this.discount)
                .priority(this.priority)
                .exclusiveRuleIds(new HashSet<>(this.exclusiveRuleIds))
                .build();
    }
}
