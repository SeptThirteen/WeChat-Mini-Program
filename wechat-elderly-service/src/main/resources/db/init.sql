CREATE DATABASE IF NOT EXISTS wechat_elderly DEFAULT CHARACTER SET utf8mb4;
USE wechat_elderly;

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT,
  address VARCHAR(255),
  community VARCHAR(100) DEFAULT NULL COMMENT '绑定社区',
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
  service_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  display_name VARCHAR(50) DEFAULT NULL COMMENT '面向用户的友好名称',
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workers (
  worker_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL,
  service_name VARCHAR(100) DEFAULT NULL COMMENT '冗余服务名称',
  scheduled_date VARCHAR(20) DEFAULT NULL COMMENT '预约日期',
  scheduled_slot VARCHAR(30) DEFAULT NULL COMMENT '预约时段',
  address VARCHAR(255) DEFAULT NULL COMMENT '服务地址',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  contact_phone VARCHAR(20) DEFAULT NULL COMMENT '用户联系电话（下单时快照）',
  rating INT DEFAULT NULL COMMENT '评分1-5',
  worker_id BIGINT DEFAULT NULL COMMENT '接单服务人员ID',
  assigned_time TIMESTAMP NULL DEFAULT NULL COMMENT '接单时间',
  status VARCHAR(20) NOT NULL,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_orders_service FOREIGN KEY (service_id) REFERENCES services(service_id),
  CONSTRAINT fk_orders_worker FOREIGN KEY (worker_id) REFERENCES workers(worker_id)
);

CREATE TABLE IF NOT EXISTS bill_queries (
  query_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  query_type VARCHAR(50) NOT NULL,
  query_params VARCHAR(255),
  result_snapshot VARCHAR(500),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_bill_queries_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS government_tasks (
  task_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  task_type VARCHAR(50) NOT NULL COMMENT '申办类型：养老补贴/医保备案等',
  task_desc VARCHAR(255) DEFAULT NULL COMMENT '申办描述',
  status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/PROCESSING/COMPLETED/REJECTED',
  submitted_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_gov_tasks_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS emergency_contacts (
  contact_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  relation VARCHAR(30) DEFAULT NULL COMMENT '关系：家属/社区服务等',
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_contacts_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO users (phone, name, age, address, community) VALUES
('13800000000', '张三', 68, '示例小区 1 号楼', '阳光社区居委会'),
('13900000000', '李四', 72, '示例小区 2 号楼', '幸福社区居委会');

INSERT INTO services (category, description, price, display_name) VALUES
('家务帮助', '上门打扫、整理房间', 39.90, '洁净到家'),
('基础陪护', '陪同就医、取药', 59.90, '就医陪护'),
('健康测量', '血压血糖基础测量', 29.90, '健康小站'),
('日间照护', '日间上门照护、陪伴服务', 49.90, '暖心陪伴'),
('外出陪同', '陪同外出就医、购物、办事', 59.90, '贴心出行'),
('代购', '代为购买日用品、药品等', 29.90, '跑腿帮买'),
('维修', '上门水电维修、家电简单维护', 39.90, '上门维修');

CREATE TABLE IF NOT EXISTS ai_query_logs (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  provider VARCHAR(20) NOT NULL COMMENT 'BAIDU / TENCENT',
  intent VARCHAR(30) DEFAULT 'free' COMMENT 'shengbao/health/bangfu/faq/free',
  query_text TEXT NOT NULL COMMENT '用户输入文本（ASR转写后）',
  response_text TEXT COMMENT 'AI回复文本',
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ai_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

INSERT INTO workers (phone, name, password) VALUES
('18000000001', '王师傅', '123456'),
('18000000002', '赵师傅', '123456');
