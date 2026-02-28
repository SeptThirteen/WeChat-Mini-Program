CREATE DATABASE IF NOT EXISTS wechat_elderly DEFAULT CHARACTER SET utf8mb4;
USE wechat_elderly;

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL,
  name VARCHAR(50) NOT NULL,
  age INT,
  address VARCHAR(255),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
  service_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category VARCHAR(50) NOT NULL,
  description VARCHAR(255) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id),
  CONSTRAINT fk_orders_service FOREIGN KEY (service_id) REFERENCES services(service_id)
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

INSERT INTO users (phone, name, age, address) VALUES
('13800000000', '张三', 68, '示例小区 1 号楼'),
('13900000000', '李四', 72, '示例小区 2 号楼');

INSERT INTO services (category, description, price) VALUES
('家务帮助', '上门打扫、整理房间', 39.90),
('基础陪护', '陪同就医、取药', 59.90),
('健康测量', '血压血糖基础测量', 29.90);
