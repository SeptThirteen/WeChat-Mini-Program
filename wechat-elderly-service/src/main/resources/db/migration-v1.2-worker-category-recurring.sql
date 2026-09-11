-- v1.2 迁移脚本：工人类别 + 长期订单
-- 执行前请备份数据库
-- 注意：MySQL 8.0 不支持 `ADD COLUMN IF NOT EXISTS`（那是 MariaDB 语法）。
-- 本脚本使用 information_schema + 动态 SQL 实现幂等，列已存在时自动跳过，可重复执行。

-- 1. workers 表增加 category 字段
SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'workers' AND COLUMN_NAME = 'category') = 0,
  'ALTER TABLE workers ADD COLUMN category VARCHAR(20) DEFAULT ''其他'' COMMENT ''人员类别: 超市老板/志愿者/社区工作人员/物业工作人员/其他''',
  'SELECT ''workers.category 已存在, 跳过'''
));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. orders 表增加长期订单字段
SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'order_type') = 0,
  'ALTER TABLE orders ADD COLUMN order_type VARCHAR(10) NOT NULL DEFAULT ''SINGLE'' COMMENT ''订单类型: SINGLE/RECURRING''',
  'SELECT ''orders.order_type 已存在, 跳过'''
));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'date_start') = 0,
  'ALTER TABLE orders ADD COLUMN date_start VARCHAR(20) DEFAULT NULL COMMENT ''长期订单开始日期''',
  'SELECT ''orders.date_start 已存在, 跳过'''
));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'date_end') = 0,
  'ALTER TABLE orders ADD COLUMN date_end VARCHAR(20) DEFAULT NULL COMMENT ''长期订单结束日期''',
  'SELECT ''orders.date_end 已存在, 跳过'''
));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND COLUMN_NAME = 'recurrence_rule') = 0,
  'ALTER TABLE orders ADD COLUMN recurrence_rule VARCHAR(50) DEFAULT NULL COMMENT ''重复规则: 每天/每周/每周一三五 等''',
  'SELECT ''orders.recurrence_rule 已存在, 跳过'''
));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 更新已有 workers 种子数据的 category（可选，按需执行）
-- UPDATE workers SET category = '物业工作人员' WHERE name = '王师傅';
-- UPDATE workers SET category = '志愿者' WHERE name = '赵师傅';
