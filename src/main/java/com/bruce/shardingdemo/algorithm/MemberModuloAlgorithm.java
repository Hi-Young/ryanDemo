package com.bruce.shardingdemo.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 会员券分表算法 — ComplexKeysShardingAlgorithm
 *
 * <p>参考永旺券中心 CouponMemberDbTableAlgorithm 实现，简化为单库多表场景。</p>
 *
 * <h3>分片策略</h3>
 * <ul>
 *   <li>分片键：member_id（主），coupon_no（备用）</li>
 *   <li>路由公式：tableIndex = shardingValue % tableNum</li>
 *   <li>物理表：coupon_give_record_00 ~ coupon_give_record_03</li>
 * </ul>
 *
 * <h3>为什么用 ComplexKeysShardingAlgorithm？</h3>
 * <p>业务上查询券记录有两种入口：按 member_id 查会员的所有券，按 coupon_no 查单张券。
 * 为了两种查询都能精确路由到单表，需要支持多分片键。coupon_no 中嵌入了 member_id 信息
 * （如末尾8位），因此也能计算出路由目标。</p>
 *
 * @author claude code
 */
@Slf4j
public class MemberModuloAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    /** 分表数量 */
    private final int tableNum;

    /** 从分片键值中截取的末尾位数（用于 coupon_no 提取 member_id 信息） */
    private final int subStrNum;

    /**
     * @param tableNum  分表数量（如4）
     * @param subStrNum coupon_no 截取末尾位数（如8），传0表示不支持 coupon_no 路由
     */
    public MemberModuloAlgorithm(int tableNum, int subStrNum) {
        this.tableNum = tableNum;
        this.subStrNum = subStrNum;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Long> shardingValue) {
        Collection<String> result = new ArrayList<>();
        Map<String, Collection<Long>> columnValues = shardingValue.getColumnNameAndShardingValuesMap();

        // 优先从 member_id 获取路由值
        Collection<Long> memberIds = columnValues.get("member_id");
        Collection<Long> couponNos = columnValues.get("coupon_no");

        if (memberIds != null && !memberIds.isEmpty()) {
            for (Long memberId : memberIds) {
                String target = doRoute(availableTargetNames, memberId);
                if (target != null) {
                    result.add(target);
                }
            }
        } else if (couponNos != null && !couponNos.isEmpty()) {
            // 从 coupon_no 末尾提取数字作为路由值
            for (Long couponNo : couponNos) {
                String numStr = String.valueOf(couponNo);
                long routeValue = Long.parseLong(
                        numStr.substring(Math.max(0, numStr.length() - subStrNum)));
                String target = doRoute(availableTargetNames, routeValue);
                if (target != null) {
                    result.add(target);
                }
            }
        } else {
            // 没有可用分片键，返回全部表（全表扫描）
            log.warn("未找到分片键 member_id 或 coupon_no，将路由到所有表");
            result.addAll(availableTargetNames);
        }

        return result;
    }

    /**
     * 核心路由逻辑
     * <pre>
     * 永旺完整公式（多库多表）：
     *   dbIndex    = value % (dbNum * tableNum) % dbNum
     *   tableIndex = (value % (dbNum * tableNum)) / dbNum % tableNum
     *
     * 简化版（单库多表，dbNum=1）：
     *   tableIndex = value % tableNum
     * </pre>
     */
    private String doRoute(Collection<String> availableTargetNames, long value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setGroupingUsed(false);

        String suffix = nf.format(value % tableNum);
        for (String each : availableTargetNames) {
            if (each.endsWith(suffix)) {
                return each;
            }
        }
        return null;
    }

    // ==================== 面试知识点 ====================
    // Q: 为什么不用 StandardShardingStrategyConfiguration（单分片键）？
    // A: 因为业务有两个查询入口（member_id 和 coupon_no），需要两个分片键都能路由。
    //    如果用 Standard，只能配一个分片键，另一个查询就会全表扫描。
    //
    // Q: coupon_no 怎么也能路由到正确的表？
    // A: 设计 coupon_no 生成规则时，末尾嵌入 member_id 的后8位。
    //    这样 coupon_no 的后8位 % tableNum == member_id % tableNum，保证路由一致。
    //
    // Q: 永旺真实场景的规模是多少？
    // A: 16库 × 32表 = 512个分片，value % 512 % 16 得库，(value % 512) / 16 % 32 得表。
}
