-- ===== 促销策略配置表设计 =====

-- 促销策略配置表
CREATE TABLE IF NOT EXISTS `promotion_strategy_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `strategy_name` varchar(100) NOT NULL COMMENT '策略名称',
  `strategy_type` varchar(50) NOT NULL COMMENT '策略类型：FULL_REDUCE/DISCOUNT/NEW_USER/MEMBER_EXCLUSIVE',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `priority` int DEFAULT '0' COMMENT '优先级：数字越大优先级越高',
  `description` varchar(500) DEFAULT NULL COMMENT '策略描述',
  `start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_strategy_name` (`strategy_name`),
  KEY `idx_strategy_type` (`strategy_type`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销策略配置表';

-- 促销规则详细配置表
CREATE TABLE IF NOT EXISTS `promotion_rule_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `strategy_id` bigint NOT NULL COMMENT '关联策略ID',
  `rule_key` varchar(100) NOT NULL COMMENT '规则键：min_amount/discount_rate/reduce_amount等',
  `rule_value` varchar(200) NOT NULL COMMENT '规则值',
  `rule_type` varchar(20) DEFAULT 'STRING' COMMENT '规则类型：STRING/NUMBER/BOOLEAN',
  `description` varchar(300) DEFAULT NULL COMMENT '规则描述',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id` (`strategy_id`),
  KEY `idx_rule_key` (`rule_key`),
  CONSTRAINT `fk_rule_strategy` FOREIGN KEY (`strategy_id`) REFERENCES `promotion_strategy_config` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销规则详细配置表';

-- 用户促销使用记录表
CREATE TABLE IF NOT EXISTS `user_promotion_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(100) NOT NULL COMMENT '用户ID',
  `order_id` varchar(100) NOT NULL COMMENT '订单ID',
  `strategy_name` varchar(100) NOT NULL COMMENT '使用的策略名称',
  `original_amount` decimal(10,2) NOT NULL COMMENT '原始金额',
  `final_amount` decimal(10,2) NOT NULL COMMENT '最终金额',
  `discount_amount` decimal(10,2) NOT NULL COMMENT '优惠金额',
  `promotion_detail` text COMMENT '促销详情JSON',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_strategy_name` (`strategy_name`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户促销使用记录表';

-- 插入初始测试数据
INSERT INTO `promotion_strategy_config` (`strategy_name`, `strategy_type`, `is_active`, `priority`, `description`, `start_time`, `end_time`) VALUES
('满减促销', 'FULL_REDUCE', 1, 100, '满100减20，满200减50，满500减100', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('打折促销', 'DISCOUNT', 1, 80, '分层打折：满300享8折，满150享85折，其他9折', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('新用户专享', 'NEW_USER', 1, 200, '新用户首单立减50，满100再减20，满300享7.5折', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('会员专享', 'MEMBER_EXCLUSIVE', 1, 150, 'VIP享9折+满300减30，SVIP享8.5折+满300减30', '2024-01-01 00:00:00', '2025-12-31 23:59:59'),
('双11特价', 'FESTIVAL', 1, 300, '双11狂欢4折+满500再减100', '2024-11-11 00:00:00', '2024-11-11 23:59:59');

-- 满减促销规则配置
INSERT INTO `promotion_rule_config` (`strategy_id`, `rule_key`, `rule_value`, `rule_type`, `description`) VALUES
(1, 'level1_min_amount', '100', 'NUMBER', '第一档最小金额'),
(1, 'level1_reduce_amount', '20', 'NUMBER', '第一档减免金额'),
(1, 'level2_min_amount', '200', 'NUMBER', '第二档最小金额'),
(1, 'level2_reduce_amount', '50', 'NUMBER', '第二档减免金额'),
(1, 'level3_min_amount', '500', 'NUMBER', '第三档最小金额'),
(1, 'level3_reduce_amount', '100', 'NUMBER', '第三档减免金额'),
(1, 'vip_extra_discount', '0.05', 'NUMBER', 'VIP额外折扣'),
(1, 'svip_extra_discount', '0.08', 'NUMBER', 'SVIP额外折扣');

-- 打折促销规则配置
INSERT INTO `promotion_rule_config` (`strategy_id`, `rule_key`, `rule_value`, `rule_type`, `description`) VALUES
(2, 'level1_min_amount', '300', 'NUMBER', '8折门槛'),
(2, 'level1_discount_rate', '0.8', 'NUMBER', '8折比例'),
(2, 'level2_min_amount', '150', 'NUMBER', '85折门槛'),
(2, 'level2_discount_rate', '0.85', 'NUMBER', '85折比例'),
(2, 'default_discount_rate', '0.9', 'NUMBER', '默认9折比例'),
(2, 'new_user_reduce', '50', 'NUMBER', '新用户立减金额'),
(2, 'vip_discount_rate', '0.95', 'NUMBER', 'VIP会员折扣'),
(2, 'svip_discount_rate', '0.92', 'NUMBER', 'SVIP会员折扣');

-- 新用户促销规则配置
INSERT INTO `promotion_rule_config` (`strategy_id`, `rule_key`, `rule_value`, `rule_type`, `description`) VALUES
(3, 'first_order_reduce', '50', 'NUMBER', '首单立减金额'),
(3, 'level1_min_amount', '100', 'NUMBER', '满减门槛'),
(3, 'level1_reduce_amount', '20', 'NUMBER', '满减金额'),
(3, 'level2_min_amount', '300', 'NUMBER', '特价门槛'),
(3, 'level2_discount_rate', '0.75', 'NUMBER', '特价折扣');

-- ===== 数据库表设计亮点 =====

/*
🎯 表设计优势：

1. 【配置灵活】促销策略完全由数据库配置驱动
   - 运营人员可随时调整规则
   - 支持时间段控制
   - 支持优先级排序

2. 【规则细分】promotion_rule_config支持复杂规则
   - 键值对存储，扩展性强
   - 支持多种数据类型
   - 便于程序动态解析

3. 【数据追踪】user_promotion_record记录所有使用情况  
   - 便于数据分析
   - 支持促销效果评估
   - 可以防止重复使用

4. 【性能优化】合理的索引设计
   - 查询性能优异
   - 支持高并发场景

💡 接下来我们会创建对应的Entity和Service来使用这些表！
*/