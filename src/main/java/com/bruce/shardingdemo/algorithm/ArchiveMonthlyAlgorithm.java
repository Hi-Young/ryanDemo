package com.bruce.shardingdemo.algorithm;

import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 按月归档分表算法 — PreciseShardingAlgorithm + RangeShardingAlgorithm
 *
 * <p>参考永旺营销中心 ArchiveMonthlyAlgorithm 实现。</p>
 *
 * <h3>分片策略</h3>
 * <ul>
 *   <li>分片键：created_at（日期类型）</li>
 *   <li>路由公式：表后缀 = yyyyMM（如 202503）</li>
 *   <li>物理表：order_record_202501 ~ order_record_202512</li>
 * </ul>
 *
 * <h3>为什么同时实现 Precise + Range？</h3>
 * <ul>
 *   <li>Precise：处理精确查询（= 某个日期），路由到单表</li>
 *   <li>Range：处理范围查询（BETWEEN 日期A AND 日期B），路由到多表</li>
 *   <li>如果只实现 Precise，范围查询会报错</li>
 * </ul>
 *
 * @author claude code
 */
@Slf4j
public class ArchiveMonthlyAlgorithm implements PreciseShardingAlgorithm<Date>, RangeShardingAlgorithm<Date> {

    private static final String DATE_FORMAT = "yyyyMM";

    /**
     * 精确分片：INSERT / 精确查询（WHERE created_at = ?）
     * 返回单个物理表名
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames,
                             PreciseShardingValue<Date> shardingValue) {
        Date date = shardingValue.getValue();
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        String suffix = format.format(date);
        String actualTableName = shardingValue.getLogicTableName() + "_" + suffix;

        if (availableTargetNames.contains(actualTableName)) {
            log.debug("日期={} 路由到表: {}", date, actualTableName);
            return actualTableName;
        }

        throw new IllegalArgumentException(
                "物理表不存在: " + actualTableName + ", 可用表: " + availableTargetNames);
    }

    /**
     * 范围分片：范围查询（WHERE created_at BETWEEN ? AND ?）
     * 返回范围内涉及的所有物理表
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         RangeShardingValue<Date> shardingValue) {
        List<String> result = new LinkedList<>();
        Range<Date> range = shardingValue.getValueRange();

        // 获取范围边界
        Date startDate = range.hasLowerBound() ? range.lowerEndpoint() : null;
        Date endDate = range.hasUpperBound() ? range.upperEndpoint() : null;

        if (startDate == null || endDate == null) {
            // 无上界或无下界，返回所有表
            log.warn("范围查询缺少边界，将路由到所有表");
            return new ArrayList<>(availableTargetNames);
        }

        // 遍历 startDate 到 endDate 之间每个月，收集涉及的物理表
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        // 设置到月初，避免遗漏
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        DateFormat format = new SimpleDateFormat(DATE_FORMAT);

        while (!calendar.getTime().after(endDate)) {
            String suffix = format.format(calendar.getTime());
            String actualTableName = shardingValue.getLogicTableName() + "_" + suffix;

            if (availableTargetNames.contains(actualTableName)) {
                result.add(actualTableName);
            }
            // 移动到下个月
            calendar.add(Calendar.MONTH, 1);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "范围内无可用物理表, start=" + startDate + ", end=" + endDate);
        }

        log.debug("范围查询 {} ~ {} 路由到表: {}", startDate, endDate, result);
        return result;
    }

    // ==================== 面试知识点 ====================
    // Q: 按时间分片的优缺点？
    // A: 优点 — 天然支持数据归档、历史数据可直接 DROP TABLE 清理、查询通常带时间范围条件。
    //    缺点 — 热点问题：最近一个月的表承受几乎所有写入压力。
    //    解决 — 可以在月份表内再按 member_id 做二级分表。
    //
    // Q: 如果物理表还没创建怎么办？（比如跨年了但没建下一年的表）
    // A: 永旺通过定时任务提前创建未来3个月的表。
    //    生产环境必须有"预建表"机制，否则写入直接报错。
    //
    // Q: 范围查询跨多表时性能如何？
    // A: ShardingSphere 会对多表并行查询后合并结果（归并引擎），
    //    但如果跨了12个月就会查12张表，所以查询条件要尽量缩小时间范围。
}
