-- =====================================================
-- 数据库死锁学习相关表结构
-- =====================================================

-- 1. 账户表 - 用于模拟转账死锁场景
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(32) NOT NULL COMMENT '账户号',
  `account_name` varchar(100) NOT NULL COMMENT '账户名',
  `balance` decimal(15,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `version` int(11) NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_no` (`account_no`),
  KEY `idx_account_name` (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表-用于模拟转账死锁';

-- 2. 库存表 - 用于模拟库存扣减死锁场景  
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `stock_quantity` int(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `reserved_quantity` int(11) NOT NULL DEFAULT 0 COMMENT '预留数量',
  `version` int(11) NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`),
  KEY `idx_product_name` (`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表-用于模拟库存扣减死锁';

-- 3. 订单锁表 - 用于模拟订单处理死锁场景
DROP TABLE IF EXISTS `order_locks`;
CREATE TABLE `order_locks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `product_ids` varchar(500) NOT NULL COMMENT '商品ID列表,逗号分隔',
  `total_amount` decimal(12,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint(2) NOT NULL DEFAULT 0 COMMENT '订单状态 0:待处理 1:处理中 2:已完成 3:已取消',
  `process_time` datetime NULL COMMENT '处理时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单锁表-用于模拟订单处理死锁';

-- =====================================================
-- 测试数据
-- =====================================================

-- 账户测试数据
INSERT INTO `account` (`account_no`, `account_name`, `balance`) VALUES
('ACC001', '张三账户', 10000.00),
('ACC002', '李四账户', 8000.00),  
('ACC003', '王五账户', 15000.00),
('ACC004', '赵六账户', 5000.00);

-- 库存测试数据
INSERT INTO `inventory` (`product_id`, `product_name`, `stock_quantity`) VALUES
(1001, 'iPhone 15 Pro', 100),
(1002, 'MacBook Pro M3', 50),
(1003, 'iPad Air', 200),
(1004, 'AirPods Pro', 300),
(1005, 'Apple Watch Series 9', 150);

-- 订单锁测试数据
INSERT INTO `order_locks` (`order_no`, `user_id`, `product_ids`, `total_amount`, `status`) VALUES
('ORD20241001001', 1, '1001,1002', 25999.00, 0),
('ORD20241001002', 2, '1003,1004', 3299.00, 0),
('ORD20241001003', 3, '1001,1005', 8999.00, 0),
('ORD20241001004', 4, '1002,1003,1004', 15999.00, 0);