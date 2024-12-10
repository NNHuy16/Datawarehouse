CREATE DATABASE IF NOT EXISTS db_controller;
USE db_controller;

ALTER DATABASE db_controller CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS process;
DROP TABLE IF EXISTS configs;
DROP TABLE IF EXISTS process_flows;

CREATE TABLE configs (
		id INT AUTO_INCREMENT PRIMARY KEY,
	  file_name VARCHAR(255),
    source_path VARCHAR(255),
		file_location VARCHAR(255),
    backup_path VARCHAR(255),
		warehouse_procedure VARCHAR(100),
    version VARCHAR(50),
    is_active TINYINT(1) UNSIGNED DEFAULT '0' COMMENT '0: inactive, 1: active',
    insert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE process (
		id INT AUTO_INCREMENT PRIMARY KEY,
		config_id INT,
    process_at VARCHAR(100) COMMENT 'crawl, staging, warehouse, datamart',
    status VARCHAR(100) COMMENT 'READY, RUNNING, FAILED, SUCCESS',
    begin_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
		
		FOREIGN KEY (config_id) REFERENCES configs(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE process_flows (
		id INT AUTO_INCREMENT PRIMARY KEY,
		current_stage VARCHAR(100) NOT NULL,
		next_stage VARCHAR(100),
		
		UNIQUE KEY unique_flow (current_stage, next_stage)
);

CREATE TABLE logs (
		id INT AUTO_INCREMENT PRIMARY KEY,
    process_id INT,
    message TEXT,
		insert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    level VARCHAR(100) COMMENT 'info, warn, error, debug',
		
		FOREIGN KEY (process_id) REFERENCES process(id) ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO process_flows (current_stage, next_stage) VALUES
('crawl', 'staging'),
('staging', 'warehouse'),
('warehouse', 'datamart'),
('datamart', NULL);

INSERT INTO configs (file_name, source_path, file_location, backup_path, warehouse_procedure, version, is_active)
VALUES
('dataLaptop_daily.csv', 'https://tiki.vn/laptop-may-vi-tinh-linh-kien/c1846', 'D:/Workspace/DataWarehouse/data/', 'D:/backup', 'insert_data_to_datawarehouse', 1, 1);

-- process 1
INSERT INTO process (config_id, process_at, status) VALUES
(1, 'staging', 'READY');

UPDATE process
SET status = 'SUCCESS'
WHERE id = 1;

INSERT INTO logs (process_id, message, level)
VALUES (1, 'Load into Staging Success', 'info');

DROP PROCEDURE IF EXISTS insert_next_process;
DELIMITER $$

CREATE PROCEDURE insert_next_process(config_id INT, current_stage VARCHAR(100))
BEGIN
    DECLARE next_stage VARCHAR(100);

    -- Lấy next_stage từ bảng `process_flows`
    SELECT f.next_stage
    INTO next_stage
    FROM process_flows f
    WHERE f.current_stage = current_stage
    LIMIT 1; 

    -- Nếu next_stage không phải NULL, thêm bản ghi mới vào bảng process
    IF next_stage IS NOT NULL THEN
        INSERT INTO process (config_id, process_at, status)
        VALUES (config_id, next_stage, 'READY');
    END IF;
END$$

DELIMITER ;

CALL insert_next_process(1, 'crawl');
