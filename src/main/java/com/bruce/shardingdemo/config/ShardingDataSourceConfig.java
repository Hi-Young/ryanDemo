package com.bruce.shardingdemo.config;

import com.bruce.shardingdemo.algorithm.ArchiveMonthlyAlgorithm;
import com.bruce.shardingdemo.algorithm.MemberModuloAlgorithm;
import com.bruce.shardingdemo.algorithm.TemplateIdPreciseAlgorithm;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ShardingSphere 分库分表配置类
 *
 * <p>参考永旺券中心 CouponMemberShardingJdbcConfig / CouponGatherShardingJdbcConfig 实现，
 * 使用 Java API 方式手动构建 ShardingDataSource。</p>
 *
 * <h3>本示例配置了 3 组分片规则：</h3>
 * <ol>
 *   <li>coupon_give_record — 按 member_id 取模分4表（ComplexKeysShardingAlgorithm）</li>
 *   <li>coupon_give_record_gather — 按 coupon_template_id 取模分4表（PreciseShardingAlgorithm）</li>
 *   <li>order_record — 按 created_at 按月分12表（Precise + Range ShardingAlgorithm）</li>
 * </ol>
 *
 * <h3>与永旺项目的对应关系</h3>
 * <pre>
 * 永旺                          → bruceDemo
 * CouponMemberShardingJdbcConfig → 本类中 coupon_give_record 规则
 * CouponGatherShardingJdbcConfig → 本类中 coupon_give_record_gather 规则
 * ArchiveMonthlyAlgorithm(营销)  → 本类中 order_record 规则
 * </pre>
 *
 * @author claude code
 */
@Slf4j
@Configuration
@MapperScan(basePackages = "com.bruce.shardingdemo.mapper",
        sqlSessionFactoryRef = "shardingSqlSessionFactory")
public class ShardingDataSourceConfig {

    /** 会员券表分表数量 */
    private static final int MEMBER_TABLE_NUM = 4;
    /** 聚合表分表数量 */
    private static final int GATHER_TABLE_NUM = 4;

    @Value("${spring.datasource.dynamic.datasource.db1.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.dynamic.datasource.db1.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.db1.password}")
    private String password;

    /**
     * 构建底层真实数据源（单库场景只有一个）
     */
    private DataSource createRealDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(2);
        ds.setConnectionTestQuery("SELECT 1");
        return ds;
    }

    /**
     * 创建 ShardingSphere 分片数据源
     *
     * <p>核心流程（与永旺一致）：</p>
     * <pre>
     * 1. 创建真实数据源 Map（单库只有一个 entry）
     * 2. 为每个逻辑表配置 TableRuleConfiguration
     * 3. 在 TableRule 上设置分片策略（算法 + 分片键）
     * 4. 汇总到 ShardingRuleConfiguration
     * 5. 调用 ShardingDataSourceFactory.createDataSource() 生成分片数据源
     * </pre>
     */
    @Bean("shardingDataSource")
    public DataSource shardingDataSource() throws SQLException {
        // 第1步：数据源 Map（key 是数据源名，单库场景就一个）
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds0", createRealDataSource());

        // 第2步：分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        // 规则1：coupon_give_record — member_id 取模分表
        shardingRuleConfig.getTableRuleConfigs().add(buildMemberTableRule());

        // 规则2：coupon_give_record_gather — template_id 精确分表
        shardingRuleConfig.getTableRuleConfigs().add(buildGatherTableRule());

        // 规则3：order_record — 按月归档分表
        shardingRuleConfig.getTableRuleConfigs().add(buildOrderTableRule());

        // 第3步：属性配置
        Properties props = new Properties();
        props.put("sql.show", true); // 开发调试：打印实际执行的 SQL 和路由信息

        log.info("=== ShardingSphere 分片数据源初始化完成 ===");
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, props);
    }

    /**
     * 规则1：会员券流水表 — ComplexKeysShardingAlgorithm
     *
     * <pre>
     * 逻辑表: coupon_give_record
     * 物理表: ds0.coupon_give_record_00 ~ ds0.coupon_give_record_03
     * 分片键: member_id, coupon_no
     * 算法:   MemberModuloAlgorithm (value % 4)
     * </pre>
     */
    private TableRuleConfiguration buildMemberTableRule() {
        // actualDataNodes: ds0.coupon_give_record_00, ds0.coupon_give_record_01, ...
        String actualDataNodes = IntStream.range(0, MEMBER_TABLE_NUM)
                .mapToObj(i -> String.format("ds0.coupon_give_record_%02d", i))
                .collect(Collectors.joining(","));

        TableRuleConfiguration rule = new TableRuleConfiguration(
                "coupon_give_record", actualDataNodes);

        // 分表策略：ComplexShardingStrategy，支持 member_id + coupon_no 双分片键
        rule.setTableShardingStrategyConfig(
                new ComplexShardingStrategyConfiguration(
                        "member_id,coupon_no",
                        new MemberModuloAlgorithm(MEMBER_TABLE_NUM, 8)));

        return rule;
    }

    /**
     * 规则2：券聚合表 — PreciseShardingAlgorithm
     *
     * <pre>
     * 逻辑表: coupon_give_record_gather
     * 物理表: ds0.coupon_give_record_gather_00 ~ ds0.coupon_give_record_gather_03
     * 分片键: coupon_template_id
     * 算法:   TemplateIdPreciseAlgorithm (templateId % 4)
     * </pre>
     */
    private TableRuleConfiguration buildGatherTableRule() {
        String actualDataNodes = IntStream.range(0, GATHER_TABLE_NUM)
                .mapToObj(i -> String.format("ds0.coupon_give_record_gather_%02d", i))
                .collect(Collectors.joining(","));

        TableRuleConfiguration rule = new TableRuleConfiguration(
                "coupon_give_record_gather", actualDataNodes);

        // 分表策略：StandardShardingStrategy，单分片键精确匹配
        rule.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration(
                        "coupon_template_id",
                        new TemplateIdPreciseAlgorithm(GATHER_TABLE_NUM)));

        return rule;
    }

    /**
     * 规则3：订单按月归档表 — Precise + Range ShardingAlgorithm
     *
     * <pre>
     * 逻辑表: order_record
     * 物理表: ds0.order_record_202501 ~ ds0.order_record_202512
     * 分片键: created_at
     * 算法:   ArchiveMonthlyAlgorithm (日期 → yyyyMM 后缀)
     * </pre>
     */
    private TableRuleConfiguration buildOrderTableRule() {
        String actualDataNodes = IntStream.rangeClosed(1, 12)
                .mapToObj(i -> String.format("ds0.order_record_2025%02d", i))
                .collect(Collectors.joining(","));

        TableRuleConfiguration rule = new TableRuleConfiguration(
                "order_record", actualDataNodes);

        ArchiveMonthlyAlgorithm algorithm = new ArchiveMonthlyAlgorithm();

        // StandardShardingStrategy 同时接受 Precise 和 Range 算法
        rule.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration(
                        "created_at",
                        algorithm,  // PreciseShardingAlgorithm
                        algorithm)); // RangeShardingAlgorithm

        return rule;
    }

    /**
     * 为 sharding mapper 创建独立的 SqlSessionFactory
     *
     * <p>注意：必须使用 MybatisSqlSessionFactoryBean（MyBatis-Plus 提供的），
     * 而不是原生的 SqlSessionFactoryBean。否则 BaseMapper 的通用方法（insert/selectList 等）
     * 不会被注入，会报 "Invalid bound statement" 错误。</p>
     */
    @Bean("shardingSqlSessionFactory")
    public SqlSessionFactory shardingSqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(shardingDataSource());

        // MyBatis-Plus 配置（必须用 MybatisConfiguration，不能用原生 Configuration）
        com.baomidou.mybatisplus.core.MybatisConfiguration configuration =
                new com.baomidou.mybatisplus.core.MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }

    /**
     * 为 sharding 数据源创建独立的事务管理器
     */
    @Bean("shardingTransactionManager")
    public DataSourceTransactionManager shardingTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(shardingDataSource());
    }


    // ==================== 面试知识点 ====================
    // Q: 为什么用 Java API 而不是 YAML 配置？
    // A: 1) 永旺项目从 Apollo 配置中心动态读取分片参数（dbNum、tableNum），需要编程式构建
    //    2) Java API 更灵活，可以根据不同环境动态调整分片策略
    //    3) 多个分片数据源可以独立配置，避免 YAML 配置膨胀
    //
    // Q: ShardingDataSource 和现有 dynamic-datasource 怎么共存？
    // A: 通过 @MapperScan 的 sqlSessionFactoryRef 隔离 —— sharding mapper 用 shardingSqlSessionFactory，
    //    其他 mapper 用默认的 SqlSessionFactory。两套数据源完全独立，互不干扰。
    //
    // Q: sql.show=true 在生产环境要关吗？
    // A: 必须关！它会打印每条 SQL 的逻辑表 → 物理表路由信息，影响性能且可能泄露表结构。
}
