-- =====================================================
-- 分库分表示例建表脚本
-- 数据库: bruce_demo (单库多表模拟)
-- Author: claude code
-- =====================================================

-- =====================================================
-- 1. 会员券流水表 (按 member_id 取模分4张表)
--    分片策略: ComplexKeysShardingAlgorithm
--    分片键: member_id / coupon_no
--    路由公式: member_id % tableNum
-- =====================================================

CREATE TABLE IF NOT EXISTS `coupon_give_record_00` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID (分片键)',
    `coupon_no` VARCHAR(32) NOT NULL COMMENT '券编号 (备用分片键)',
    `coupon_template_id` BIGINT(20) NOT NULL COMMENT '券模板ID',
    `coupon_name` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '券名称',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用 1-已使用 2-已过期',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_coupon_no` (`coupon_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员券发放记录_00';

CREATE TABLE IF NOT EXISTS `coupon_give_record_01` LIKE `coupon_give_record_00`;
CREATE TABLE IF NOT EXISTS `coupon_give_record_02` LIKE `coupon_give_record_00`;
CREATE TABLE IF NOT EXISTS `coupon_give_record_03` LIKE `coupon_give_record_00`;

-- =====================================================
-- 2. 券聚合表 (按 coupon_template_id 取模分4张表)
--    分片策略: PreciseShardingAlgorithm
--    分片键: coupon_template_id
--    路由公式: coupon_template_id % tableNum
-- =====================================================

CREATE TABLE IF NOT EXISTS `coupon_give_record_gather_00` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `coupon_template_id` BIGINT(20) NOT NULL COMMENT '券模板ID (分片键)',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `coupon_no` VARCHAR(32) NOT NULL COMMENT '券编号',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用 1-已使用 2-已过期',
    `give_count` INT(11) NOT NULL DEFAULT 1 COMMENT '发放数量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`coupon_template_id`),
    KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='券聚合记录_00';

CREATE TABLE IF NOT EXISTS `coupon_give_record_gather_01` LIKE `coupon_give_record_gather_00`;
CREATE TABLE IF NOT EXISTS `coupon_give_record_gather_02` LIKE `coupon_give_record_gather_00`;
CREATE TABLE IF NOT EXISTS `coupon_give_record_gather_03` LIKE `coupon_give_record_gather_00`;

-- =====================================================
-- 3. 订单按月归档表 (按 created_at 按月分表)
--    分片策略: PreciseShardingAlgorithm + RangeShardingAlgorithm
--    分片键: created_at
--    路由公式: 日期格式化为 yyyyMM 作为表后缀
-- =====================================================

CREATE TABLE IF NOT EXISTS `order_record_202501` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `member_id` BIGINT(20) NOT NULL COMMENT '会员ID',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已取消',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 (分片键)',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单记录_202501';

CREATE TABLE IF NOT EXISTS `order_record_202502` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202503` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202504` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202505` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202506` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202507` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202508` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202509` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202510` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202511` LIKE `order_record_202501`;
CREATE TABLE IF NOT EXISTS `order_record_202512` LIKE `order_record_202501`;
