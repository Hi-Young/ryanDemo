package com.bruce.shardingdemo;

import com.bruce.shardingdemo.algorithm.ArchiveMonthlyAlgorithm;
import com.bruce.shardingdemo.algorithm.MemberModuloAlgorithm;
import com.bruce.shardingdemo.algorithm.TemplateIdPreciseAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import com.google.common.collect.Range;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 分片算法纯单元测试 — 不依赖 Spring 容器
 *
 * <p>直接构造算法对象，验证路由结果是否正确。
 * 面试时可以直接拿这些测试用例来讲解路由逻辑。</p>
 *
 * @author claude code
 */
public class ShardingAlgorithmTest {

    // ======================== 1. MemberModuloAlgorithm 测试 ========================

    @Test
    public void testMemberModulo_memberId路由() {
        MemberModuloAlgorithm algorithm = new MemberModuloAlgorithm(4, 8);
        List<String> tables = Arrays.asList(
                "coupon_give_record_00", "coupon_give_record_01",
                "coupon_give_record_02", "coupon_give_record_03");

        // memberId=100 → 100 % 4 = 0 → coupon_give_record_00
        assertMemberRoute(algorithm, tables, "member_id", 100L, "coupon_give_record_00");

        // memberId=101 → 101 % 4 = 1 → coupon_give_record_01
        assertMemberRoute(algorithm, tables, "member_id", 101L, "coupon_give_record_01");

        // memberId=102 → 102 % 4 = 2 → coupon_give_record_02
        assertMemberRoute(algorithm, tables, "member_id", 102L, "coupon_give_record_02");

        // memberId=103 → 103 % 4 = 3 → coupon_give_record_03
        assertMemberRoute(algorithm, tables, "member_id", 103L, "coupon_give_record_03");

        // memberId=104 → 104 % 4 = 0 → coupon_give_record_00（循环回来）
        assertMemberRoute(algorithm, tables, "member_id", 104L, "coupon_give_record_00");
    }

    @Test
    public void testMemberModulo_couponNo备用路由() {
        MemberModuloAlgorithm algorithm = new MemberModuloAlgorithm(4, 8);
        List<String> tables = Arrays.asList(
                "coupon_give_record_00", "coupon_give_record_01",
                "coupon_give_record_02", "coupon_give_record_03");

        // couponNo=20250100 → 末8位=20250100 → 20250100 % 4 = 0 → coupon_give_record_00
        assertMemberRoute(algorithm, tables, "coupon_no", 20250100L, "coupon_give_record_00");

        // couponNo=20250101 → 末8位=20250101 → 20250101 % 4 = 1 → coupon_give_record_01
        assertMemberRoute(algorithm, tables, "coupon_no", 20250101L, "coupon_give_record_01");
    }

    @Test
    public void testMemberModulo_无分片键返回全部表() {
        MemberModuloAlgorithm algorithm = new MemberModuloAlgorithm(4, 8);
        List<String> tables = Arrays.asList(
                "coupon_give_record_00", "coupon_give_record_01",
                "coupon_give_record_02", "coupon_give_record_03");

        // 没有分片键 → 返回所有表
        Map<String, Collection<Long>> columnValues = new HashMap<>();
        ComplexKeysShardingValue<Long> shardingValue = new ComplexKeysShardingValue<>(
                "coupon_give_record", columnValues, Collections.emptyMap());

        Collection<String> result = algorithm.doSharding(tables, shardingValue);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void testMemberModulo_数据分布均匀性() {
        MemberModuloAlgorithm algorithm = new MemberModuloAlgorithm(4, 8);
        List<String> tables = Arrays.asList(
                "coupon_give_record_00", "coupon_give_record_01",
                "coupon_give_record_02", "coupon_give_record_03");

        // 验证 1000 个 memberId 均匀分布到 4 张表
        Map<String, Integer> distribution = new HashMap<>();
        for (long i = 1; i <= 1000; i++) {
            Map<String, Collection<Long>> columnValues = new HashMap<>();
            columnValues.put("member_id", Collections.singletonList(i));
            ComplexKeysShardingValue<Long> sv = new ComplexKeysShardingValue<>(
                    "coupon_give_record", columnValues, Collections.emptyMap());
            Collection<String> result = algorithm.doSharding(tables, sv);
            String table = result.iterator().next();
            distribution.merge(table, 1, Integer::sum);
        }

        // 每张表应该分到 250 条（1000/4）
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            Assert.assertEquals("表 " + entry.getKey() + " 数据分布不均匀",
                    250, entry.getValue().intValue());
        }
    }

    // ======================== 2. TemplateIdPreciseAlgorithm 测试 ========================

    @Test
    public void testTemplateIdPrecise_路由正确() {
        TemplateIdPreciseAlgorithm algorithm = new TemplateIdPreciseAlgorithm(4);
        List<String> tables = Arrays.asList(
                "coupon_give_record_gather_00", "coupon_give_record_gather_01",
                "coupon_give_record_gather_02", "coupon_give_record_gather_03");

        // templateId=1000 → 1000 % 4 = 0 → gather_00
        assertPreciseRoute(algorithm, tables, 1000L, "coupon_give_record_gather_00");

        // templateId=1001 → 1001 % 4 = 1 → gather_01
        assertPreciseRoute(algorithm, tables, 1001L, "coupon_give_record_gather_01");

        // templateId=1002 → 1002 % 4 = 2 → gather_02
        assertPreciseRoute(algorithm, tables, 1002L, "coupon_give_record_gather_02");

        // templateId=1003 → 1003 % 4 = 3 → gather_03
        assertPreciseRoute(algorithm, tables, 1003L, "coupon_give_record_gather_03");

        // templateId=1004 → 1004 % 4 = 0 → gather_00（循环）
        assertPreciseRoute(algorithm, tables, 1004L, "coupon_give_record_gather_00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTemplateIdPrecise_表不存在抛异常() {
        TemplateIdPreciseAlgorithm algorithm = new TemplateIdPreciseAlgorithm(4);
        // 只给一张表，但 templateId=1 → 1%4=1 → 找不到 _01 后缀的表
        List<String> tables = Collections.singletonList("coupon_give_record_gather_00");

        PreciseShardingValue<Long> sv = new PreciseShardingValue<>(
                "coupon_give_record_gather", "coupon_template_id", 1L);
        algorithm.doSharding(tables, sv);
    }

    // ======================== 3. ArchiveMonthlyAlgorithm 测试 ========================

    @Test
    public void testArchiveMonthly_精确路由() throws ParseException {
        ArchiveMonthlyAlgorithm algorithm = new ArchiveMonthlyAlgorithm();
        List<String> tables = buildMonthlyTables();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 2025-01-15 → order_record_202501
        assertMonthlyPreciseRoute(algorithm, tables, sdf.parse("2025-01-15"), "order_record_202501");

        // 2025-06-01 → order_record_202506
        assertMonthlyPreciseRoute(algorithm, tables, sdf.parse("2025-06-01"), "order_record_202506");

        // 2025-12-31 → order_record_202512
        assertMonthlyPreciseRoute(algorithm, tables, sdf.parse("2025-12-31"), "order_record_202512");
    }

    @Test
    public void testArchiveMonthly_范围路由() throws ParseException {
        ArchiveMonthlyAlgorithm algorithm = new ArchiveMonthlyAlgorithm();
        List<String> tables = buildMonthlyTables();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = sdf.parse("2025-03-01");
        Date end = sdf.parse("2025-05-31");

        RangeShardingValue<Date> sv = new RangeShardingValue<>(
                "order_record", "created_at", Range.closed(start, end));

        Collection<String> result = algorithm.doSharding(tables, sv);

        // 3月~5月，应该路由到 3 张表
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("order_record_202503"));
        Assert.assertTrue(result.contains("order_record_202504"));
        Assert.assertTrue(result.contains("order_record_202505"));
    }

    @Test
    public void testArchiveMonthly_单月范围路由() throws ParseException {
        ArchiveMonthlyAlgorithm algorithm = new ArchiveMonthlyAlgorithm();
        List<String> tables = buildMonthlyTables();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = sdf.parse("2025-07-10");
        Date end = sdf.parse("2025-07-20");

        RangeShardingValue<Date> sv = new RangeShardingValue<>(
                "order_record", "created_at", Range.closed(start, end));

        Collection<String> result = algorithm.doSharding(tables, sv);

        // 同月范围，只路由到 1 张表
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("order_record_202507"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArchiveMonthly_物理表不存在抛异常() throws ParseException {
        ArchiveMonthlyAlgorithm algorithm = new ArchiveMonthlyAlgorithm();
        List<String> tables = buildMonthlyTables();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 2024年的表不存在
        Date date = sdf.parse("2024-01-15");

        PreciseShardingValue<Date> sv = new PreciseShardingValue<>(
                "order_record", "created_at", date);
        algorithm.doSharding(tables, sv);
    }

    // ======================== 辅助方法 ========================

    private void assertMemberRoute(MemberModuloAlgorithm algorithm, List<String> tables,
                                   String column, Long value, String expectedTable) {
        Map<String, Collection<Long>> columnValues = new HashMap<>();
        columnValues.put(column, Collections.singletonList(value));
        ComplexKeysShardingValue<Long> sv = new ComplexKeysShardingValue<>(
                "coupon_give_record", columnValues, Collections.emptyMap());

        Collection<String> result = algorithm.doSharding(tables, sv);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(expectedTable, result.iterator().next());
    }

    private void assertPreciseRoute(TemplateIdPreciseAlgorithm algorithm,
                                    List<String> tables, Long templateId, String expectedTable) {
        PreciseShardingValue<Long> sv = new PreciseShardingValue<>(
                "coupon_give_record_gather", "coupon_template_id", templateId);
        String result = algorithm.doSharding(tables, sv);
        Assert.assertEquals(expectedTable, result);
    }

    private void assertMonthlyPreciseRoute(ArchiveMonthlyAlgorithm algorithm,
                                           List<String> tables, Date date, String expectedTable) {
        PreciseShardingValue<Date> sv = new PreciseShardingValue<>(
                "order_record", "created_at", date);
        String result = algorithm.doSharding(tables, sv);
        Assert.assertEquals(expectedTable, result);
    }

    private List<String> buildMonthlyTables() {
        List<String> tables = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            tables.add(String.format("order_record_2025%02d", i));
        }
        return tables;
    }
}
