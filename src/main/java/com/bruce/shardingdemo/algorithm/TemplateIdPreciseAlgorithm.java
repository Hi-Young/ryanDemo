package com.bruce.shardingdemo.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.text.NumberFormat;
import java.util.Collection;

/**
 * 券聚合表精确分表算法 — PreciseShardingAlgorithm
 *
 * <p>参考永旺券中心 CouponGatherDbTableHitAlgorithm 实现，简化为单库多表场景。</p>
 *
 * <h3>分片策略</h3>
 * <ul>
 *   <li>分片键：coupon_template_id</li>
 *   <li>路由公式：tableIndex = templateId % tableNum</li>
 *   <li>物理表：coupon_give_record_gather_00 ~ coupon_give_record_gather_03</li>
 * </ul>
 *
 * <h3>为什么券聚合库用 PreciseShardingAlgorithm？</h3>
 * <p>聚合表的核心查询场景是"查某个券模板下的所有发放记录"，只需要一个分片键
 * （coupon_template_id），且是精确匹配（=），不涉及范围查询，
 * 所以用最简单的 PreciseShardingAlgorithm 就够了。</p>
 *
 * @author claude code
 */
@Slf4j
public class TemplateIdPreciseAlgorithm implements PreciseShardingAlgorithm<Long> {

    /** 分表数量 */
    private final int tableNum;

    public TemplateIdPreciseAlgorithm(int tableNum) {
        this.tableNum = tableNum;
    }

    @Override
    public String doSharding(Collection<String> availableTargetNames,
                             PreciseShardingValue<Long> shardingValue) {
        Long templateId = shardingValue.getValue();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setGroupingUsed(false);

        // 核心路由公式
        // 永旺完整公式（多库多表）：
        //   dbIndex    = templateId % (dbNum * tableNum) % dbNum
        //   tableIndex = (templateId % (dbNum * tableNum)) / dbNum % tableNum
        // 简化版（单库多表，dbNum=1）：
        //   tableIndex = templateId % tableNum
        String suffix = nf.format(templateId % tableNum);

        for (String each : availableTargetNames) {
            if (each.endsWith(suffix)) {
                log.debug("templateId={} 路由到表: {}", templateId, each);
                return each;
            }
        }

        throw new IllegalArgumentException(
                "未找到匹配的物理表, templateId=" + templateId + ", suffix=" + suffix);
    }

    // ==================== 面试知识点 ====================
    // Q: PreciseShardingAlgorithm 和 ComplexKeysShardingAlgorithm 怎么选？
    // A: 单分片键 + 精确查询（=、IN） → Precise
    //    多分片键 → Complex
    //    需要范围查询（BETWEEN、>、<） → 额外实现 RangeShardingAlgorithm
    //
    // Q: 永旺聚合库的真实规模是多少？
    // A: 8库 × 8表 = 64个分片，templateId % 64 % 8 得库，(templateId % 64) / 8 % 8 得表。
    //
    // Q: 为什么聚合库比会员库分片少？
    // A: 券模板数量远小于会员数量。会员级别的券记录可能上亿，
    //    但券模板只有几千个，所以聚合库不需要那么多分片。
}
