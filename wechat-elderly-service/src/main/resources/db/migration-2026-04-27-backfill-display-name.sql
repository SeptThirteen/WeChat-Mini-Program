-- 数据库补救迁移：兼容历史库补齐 services.display_name
-- 执行日期：2026-04-27
-- 用途：
-- 1) 若 display_name 列不存在则自动新增
-- 2) 仅为未设置 display_name 的服务回填默认友好名

USE wechat_elderly;

-- 1. 安全新增 display_name（兼容不支持 ADD COLUMN IF NOT EXISTS 的版本）
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'services'
    AND COLUMN_NAME = 'display_name'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE services ADD COLUMN display_name VARCHAR(50) DEFAULT NULL COMMENT ''面向用户的友好名称'' AFTER price',
  'SELECT ''display_name already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 回填默认友好名（仅处理为空的记录，不覆盖已有自定义）
UPDATE services SET display_name = '洁净到家' WHERE category = '家务帮助' AND (display_name IS NULL OR display_name = '');
UPDATE services SET display_name = '就医陪护' WHERE category = '基础陪护' AND (display_name IS NULL OR display_name = '');
UPDATE services SET display_name = '健康小站' WHERE category = '健康测量' AND (display_name IS NULL OR display_name = '');
UPDATE services SET display_name = '暖心陪伴' WHERE category = '日间照护' AND (display_name IS NULL OR display_name = '');
UPDATE services SET display_name = '贴心出行' WHERE category = '外出陪同' AND (display_name IS NULL OR display_name = '');
UPDATE services SET display_name = '跑腿帮买' WHERE category = '代购' AND (display_name IS NULL OR display_name = '');
UPDATE services SET display_name = '上门维修' WHERE category = '维修' AND (display_name IS NULL OR display_name = '');

-- 3. 验证
SELECT service_id, category, display_name, description, price
FROM services
ORDER BY service_id;
