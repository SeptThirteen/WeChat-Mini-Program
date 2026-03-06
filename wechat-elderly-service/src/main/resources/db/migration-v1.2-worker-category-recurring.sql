-- v1.2 迁移脚本：工人类别 + 长期订单
-- 执行前请备份数据库

-- 1. workers 表增加 category 字段
ALTER TABLE workers ADD COLUMN IF NOT EXISTS category VARCHAR(20) DEFAULT '其他' COMMENT '人员类别: 超市老板/志愿者/社区工作人员/物业工作人员/其他';

-- 2. orders 表增加长期订单字段
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_type VARCHAR(10) NOT NULL DEFAULT 'SINGLE' COMMENT '订单类型: SINGLE/RECURRING';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS date_start VARCHAR(20) DEFAULT NULL COMMENT '长期订单开始日期';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS date_end VARCHAR(20) DEFAULT NULL COMMENT '长期订单结束日期';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS recurrence_rule VARCHAR(50) DEFAULT NULL COMMENT '重复规则: 每天/每周/每周一三五 等';

-- 3. 更新已有 workers 种子数据的 category（可选，按需执行）
-- UPDATE workers SET category = '物业工作人员' WHERE name = '王师傅';
-- UPDATE workers SET category = '志愿者' WHERE name = '赵师傅';
