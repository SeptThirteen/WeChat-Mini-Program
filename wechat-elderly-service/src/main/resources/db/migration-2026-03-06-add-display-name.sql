-- 数据库迁移脚本：添加服务友好名称
-- 执行日期：2026-03-06
-- 用途：为 services 表添加 display_name 字段并更新现有数据

USE wechat_elderly;

-- 1. 添加 display_name 字段
ALTER TABLE services 
ADD COLUMN display_name VARCHAR(50) DEFAULT NULL COMMENT '面向用户的友好名称' 
AFTER price;

-- 2. 更新现有服务的友好名称
UPDATE services SET display_name = '洁净到家' WHERE category = '家务帮助';
UPDATE services SET display_name = '就医陪护' WHERE category = '基础陪护';
UPDATE services SET display_name = '健康小站' WHERE category = '健康测量';
UPDATE services SET display_name = '暖心陪伴' WHERE category = '日间照护';
UPDATE services SET display_name = '贴心出行' WHERE category = '外出陪同';
UPDATE services SET display_name = '跑腿帮买' WHERE category = '代购';
UPDATE services SET display_name = '上门维修' WHERE category = '维修';

-- 3. 验证更新结果
SELECT service_id, category, display_name, description, price FROM services;
