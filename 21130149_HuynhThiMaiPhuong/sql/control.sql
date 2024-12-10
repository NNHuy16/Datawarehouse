CREATE DATABASE IF NOT EXISTS db_controller;
USE db_controller;
ALTER DATABASE db_controller CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SET GLOBAL local_infile = 1;

DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS process;
DROP TABLE IF EXISTS configs;
DROP TABLE IF EXISTS process_flows;

--  Tạo bảng configs
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


-- Tạo bảng process
CREATE TABLE process (
		id INT AUTO_INCREMENT PRIMARY KEY,
		config_id INT,
    process_at VARCHAR(100) COMMENT 'craw, staging, warehouse, datamart',
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

INSERT INTO process_flows (current_stage, next_stage) VALUES
('crawl', 'staging'),
('staging', 'warehouse'),
('warehouse', 'datamart'),
('datamart', NULL);

-- Tạo bảng logs
CREATE TABLE logs (
		id INT AUTO_INCREMENT PRIMARY KEY,
    process_id INT,
    message TEXT,
		insert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    level VARCHAR(100) COMMENT 'info, warn, error, debug'
);

INSERT INTO configs (file_name, source_path, file_location, backup_path, warehouse_procedure, version, is_active)
VALUES
('dataLaptop_daily.csv', 'https://tiki.vn/laptop-may-vi-tinh-linh-kien/c1846', 'D:/Workspace/DataWarehouse/data/', 'D:/backup', 'insert_data_to_datawarehouse', 1, 1);



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
CALL insert_next_process(1, 'staging');


-- Tạo bảng tạm
    DROP TABLE IF EXISTS db_controller.temp_staging;
    CREATE TABLE db_controller.temp_staging (
        `id` TEXT,
        `sku` TEXT,
        `product_name` TEXT,
        `short_description` TEXT,
        `price` TEXT,
        `list_price` TEXT,
        `original_price` TEXT,
        `discount` TEXT,
        `discount_rate` TEXT,
        `all_time_quantity_sold` TEXT,
        `rating_average` TEXT,
        `review_count` TEXT,
        `inventory_status` TEXT,
        `stock_item_qty` TEXT,
        `stock_item_max_sale_qty` TEXT,
        `brand_id` TEXT,
        `brand_name` TEXT,
        `url_key` TEXT,
        `url_path` TEXT,
        `thumbnail_url` TEXT,
        `options` TEXT,
        `specifications` TEXT,
        `variations` TEXT
    );

    ALTER TABLE db_controller.temp_staging ADD INDEX(id(255));



-- Tạo thủ tục con để tạo tên file động
DELIMITER //
DROP PROCEDURE IF EXISTS GenerateFilePath;
CREATE PROCEDURE GenerateFilePath(IN target_date DATE, OUT file_path VARCHAR(500))
BEGIN
    DECLARE base_file_path VARCHAR(500);

    -- Lấy file_path từ bảng configs
    SELECT file_location INTO base_file_path
    FROM configs
    WHERE is_active = 1 LIMIT 1;

    -- Kiểm tra nếu không có file_path
    IF base_file_path IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: No active file_location found.'; -- Lỗi không có file_location
    END IF;

    -- Tạo tên file động với định dạng 'dataLaptop_yyyymmdd.csv'
    SET file_path = CONCAT(base_file_path, 'dataLaptop_', DATE_FORMAT(target_date, '%Y%m%d'), '.csv');
END //


-- Tạo proc để load dữ liệu từ file lên  bảng tạm 
DELIMITER //
DROP PROCEDURE IF EXISTS LoadDataIntoTempStaging;
CREATE PROCEDURE LoadDataIntoTempStaging(IN target_date DATE)
BEGIN
    DECLARE file_path VARCHAR(500);
    DECLARE load_sql VARCHAR(1000);
    DECLARE csv_file_path VARCHAR(500);

    -- Gọi thủ tục con GenerateFilePath để tạo tên file
    CALL GenerateFilePath(target_date, csv_file_path);

    -- Kiểm tra nếu file_path không được gán giá trị
    IF csv_file_path IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: File path generation failed.'; -- Lỗi nếu không tạo được file path
    END IF;

    -- Tạo câu lệnh SQL động cho LOAD DATA LOCAL INFILE
    SET load_sql = CONCAT(
        "LOAD DATA LOCAL INFILE '", csv_file_path, "' ",
        "INTO TABLE temp_staging ",
        "FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' ",
        "LINES TERMINATED BY '\\n' IGNORE 1 ROWS "
    );

    -- Trả về câu lệnh SQL động
    SELECT load_sql AS dynamic_sql;
END //

DELIMITER ;
CALL LoadDataIntoTempStaging('2024-12-09');

LOAD DATA LOCAL INFILE 'D:/dataLaptop_20241209.csv' INTO TABLE temp_staging FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS 
DELETE FROM temp_staging;

-- Xử lý dữ liệu trong bảng tạm 
DROP PROCEDURE IF EXISTS CleanTempStagingData;
DELIMITER //
CREATE PROCEDURE CleanTempStagingData()
BEGIN
    UPDATE db_controller.temp_staging
    SET 
        id = COALESCE(id, '0'),
        sku = COALESCE(TRIM(sku), 'N/A'),
        product_name = COALESCE(TRIM(product_name), 'N/A'),
        short_description = COALESCE(TRIM(short_description), 'N/A'),
        price = COALESCE(NULLIF(TRIM(price), ''), '0.00'),
        list_price = COALESCE(NULLIF(TRIM(list_price), ''), '0.00'),
        original_price = COALESCE(NULLIF(TRIM(original_price), ''), '0.00'),
        discount = COALESCE(NULLIF(TRIM(discount), ''), '0.00'),
        discount_rate = COALESCE(NULLIF(TRIM(discount_rate), ''), '0.00'),
        all_time_quantity_sold = COALESCE(NULLIF(TRIM(all_time_quantity_sold), ''), '0'),
        rating_average = COALESCE(NULLIF(TRIM(rating_average), ''), '0.00'),
        review_count = COALESCE(NULLIF(TRIM(review_count), ''), '0'),
        inventory_status = COALESCE(TRIM(inventory_status), 'N/A'),
        stock_item_qty = COALESCE(NULLIF(TRIM(stock_item_qty), ''), '0'),
        stock_item_max_sale_qty = COALESCE(NULLIF(TRIM(stock_item_max_sale_qty), ''), '0'),
        brand_id = COALESCE(NULLIF(TRIM(brand_id), ''), '0'),
        brand_name = COALESCE(TRIM(brand_name), 'N/A'),
        url_key = COALESCE(LEFT(TRIM(url_key), 255), 'N/A'),
        url_path = COALESCE(LEFT(TRIM(url_path), 255), 'N/A'),
        thumbnail_url = COALESCE(TRIM(thumbnail_url), 'N/A'),
        options = COALESCE(CASE 
            WHEN JSON_VALID(REPLACE(TRIM(options), "'", '"')) THEN REPLACE(TRIM(options), "'", '"')
            ELSE '"N/A"'
        END, '"N/A"'),
        specifications = COALESCE(CASE 
            WHEN JSON_VALID(REPLACE(TRIM(specifications), "'", '"')) THEN REPLACE(TRIM(specifications), "'", '"')
            ELSE '"N/A"'
        END, '"N/A"'),
        variations = COALESCE(CASE 
            WHEN JSON_VALID(REPLACE(TRIM(variations), "'", '"')) THEN REPLACE(TRIM(variations), "'", '"')
            ELSE '"N/A"'
        END, '"N/A"')
    WHERE id IS NOT NULL;
END //

DELIMITER ;