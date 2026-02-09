package com.bruce.shardingdemo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bruce.shardingdemo.entity.CouponGiveRecord;
import com.bruce.shardingdemo.entity.CouponGiveRecordGather;
import com.bruce.shardingdemo.entity.OrderRecord;
import com.bruce.shardingdemo.mapper.CouponGiveRecordGatherMapper;
import com.bruce.shardingdemo.mapper.CouponGiveRecordMapper;
import com.bruce.shardingdemo.mapper.OrderRecordMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 分库分表集成测试 — 依赖 MySQL + Spring 容器
 *
 * <p>验证 ShardingSphere 路由 + 实际 MySQL 读写。
 * 运行前确保 MySQL 中已执行 sharding_tables.sql 建表。</p>
 *
 * <p>开启 sql.show=true 后，可以在控制台看到实际路由到的物理表名。</p>
 *
 * @author claude code
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("dev")
public class ShardingIntegrationTest {

    @Autowired
    private CouponGiveRecordMapper couponGiveRecordMapper;

    @Autowired
    private CouponGiveRecordGatherMapper gatherMapper;

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    // ======================== 1. 会员券表：按 member_id 分表 ========================

    /**
     * 测试：不同 memberId 写入不同的物理表
     *
     * memberId=100 → 100%4=0 → coupon_give_record_00
     * memberId=101 → 101%4=1 → coupon_give_record_01
     * memberId=102 → 102%4=2 → coupon_give_record_02
     * memberId=103 → 103%4=3 → coupon_give_record_03
     */
    @Test
    public void test_会员券_写入不同表() {
        for (int i = 0; i < 4; i++) {
            CouponGiveRecord record = new CouponGiveRecord();
            record.setMemberId(100L + i);
            record.setCouponNo(20250100L + i);
            record.setCouponTemplateId(1001L);
            record.setCouponName("测试券-" + i);
            record.setStatus(0);
            // 控制台应该能看到 4 条 INSERT 分别路由到 _00 ~ _03
            couponGiveRecordMapper.insert(record);
            Assert.assertNotNull("插入后应有自增ID", record.getId());
        }
    }

    /**
     * 测试：按 memberId 查询能路由到正确的单表
     */
    @Test
    public void test_会员券_按memberId精确查询() {
        // 先写入
        CouponGiveRecord record = new CouponGiveRecord();
        record.setMemberId(200L); // 200%4=0 → _00
        record.setCouponNo(20250200L);
        record.setCouponTemplateId(2001L);
        record.setCouponName("精确查询测试券");
        record.setStatus(0);
        couponGiveRecordMapper.insert(record);

        // 再查询 — 应只查 coupon_give_record_00
        LambdaQueryWrapper<CouponGiveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponGiveRecord::getMemberId, 200L);
        List<CouponGiveRecord> results = couponGiveRecordMapper.selectList(wrapper);

        Assert.assertFalse("应查到至少一条记录", results.isEmpty());
        Assert.assertEquals(Long.valueOf(200L), results.get(0).getMemberId());
    }

    // ======================== 2. 券聚合表：按 templateId 分表 ========================

    /**
     * 测试：不同 templateId 写入不同的物理表
     *
     * templateId=1000 → 1000%4=0 → gather_00
     * templateId=1001 → 1001%4=1 → gather_01
     */
    @Test
    public void test_聚合表_写入不同表() {
        for (int i = 0; i < 4; i++) {
            CouponGiveRecordGather gather = new CouponGiveRecordGather();
            gather.setCouponTemplateId(1000L + i);
            gather.setMemberId(100L + i);
            gather.setCouponNo("GATHER-" + i);
            gather.setStatus(0);
            gather.setGiveCount(1);
            // 控制台应该看到 4 条 INSERT 分别路由到 gather_00 ~ gather_03
            gatherMapper.insert(gather);
            Assert.assertNotNull(gather.getId());
        }
    }

    /**
     * 测试：按 templateId 查询能路由到单表
     */
    @Test
    public void test_聚合表_按templateId精确查询() {
        CouponGiveRecordGather gather = new CouponGiveRecordGather();
        gather.setCouponTemplateId(2000L); // 2000%4=0 → gather_00
        gather.setMemberId(300L);
        gather.setCouponNo("GATHER-QUERY-TEST");
        gather.setStatus(0);
        gather.setGiveCount(5);
        gatherMapper.insert(gather);

        LambdaQueryWrapper<CouponGiveRecordGather> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponGiveRecordGather::getCouponTemplateId, 2000L);
        List<CouponGiveRecordGather> results = gatherMapper.selectList(wrapper);

        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(Long.valueOf(2000L), results.get(0).getCouponTemplateId());
    }

    // ======================== 3. 订单表：按月归档 ========================

    /**
     * 测试：不同月份的订单写入不同的物理表
     */
    @Test
    public void test_订单表_写入不同月份表() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 1月订单 → order_record_202501
        insertOrder("ORD-202501-001", 100L, new BigDecimal("99.00"),
                sdf.parse("2025-01-15 10:00:00"));

        // 6月订单 → order_record_202506
        insertOrder("ORD-202506-001", 101L, new BigDecimal("199.00"),
                sdf.parse("2025-06-20 14:30:00"));

        // 12月订单 → order_record_202512
        insertOrder("ORD-202512-001", 102L, new BigDecimal("299.00"),
                sdf.parse("2025-12-25 18:00:00"));
    }

    /**
     * 测试：按精确日期查询路由到单表
     */
    @Test
    public void test_订单表_按日期精确查询() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date marchDate = sdf.parse("2025-03-10 12:00:00");

        // 先插入3月数据
        insertOrder("ORD-202503-QUERY", 200L, new BigDecimal("50.00"), marchDate);

        // 精确查3月 — 应只查 order_record_202503
        LambdaQueryWrapper<OrderRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRecord::getCreatedAt, marchDate);
        List<OrderRecord> results = orderRecordMapper.selectList(wrapper);

        Assert.assertFalse("应查到3月订单", results.isEmpty());
    }

    /**
     * 测试：范围查询跨多个月份表
     */
    @Test
    public void test_订单表_范围查询跨月() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 先在4月和5月各插入一条
        insertOrder("ORD-202504-RANGE", 300L, new BigDecimal("100.00"),
                sdf.parse("2025-04-15 10:00:00"));
        insertOrder("ORD-202505-RANGE", 301L, new BigDecimal("200.00"),
                sdf.parse("2025-05-15 10:00:00"));

        // 范围查 4月~5月 — 应路由到 order_record_202504 和 order_record_202505
        LambdaQueryWrapper<OrderRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(OrderRecord::getCreatedAt,
                sdf.parse("2025-04-01 00:00:00"),
                sdf.parse("2025-05-31 23:59:59"));
        List<OrderRecord> results = orderRecordMapper.selectList(wrapper);

        Assert.assertTrue("应查到至少2条跨月订单", results.size() >= 2);
    }

    private void insertOrder(String orderNo, Long memberId, BigDecimal amount, Date createdAt) {
        OrderRecord order = new OrderRecord();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setAmount(amount);
        order.setStatus(0);
        order.setCreatedAt(createdAt);
        orderRecordMapper.insert(order);
        Assert.assertNotNull(order.getId());
    }
}
