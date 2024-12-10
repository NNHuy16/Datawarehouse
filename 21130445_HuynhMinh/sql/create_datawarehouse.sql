CREATE DATABASE IF NOT EXISTS db_datawarehouse;
USE db_datawarehouse;

ALTER DATABASE db_datawarehouse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP TABLE IF EXISTS fact_sales;
DROP TABLE IF EXISTS dim_products;
DROP TABLE IF EXISTS dim_manufacturers;
DROP TABLE IF EXISTS dim_dates;

CREATE TABLE dim_dates (
    date_sk INT PRIMARY KEY,               -- Khóa chính, sử dụng cho định danh ngày
    full_date DATE,                        -- Ngày đầy đủ
    day_since_2005 INT,                    -- Số ngày kể từ năm 2005
    month_since_2005 INT,                  -- Số tháng kể từ năm 2005
    day_of_week VARCHAR(10),                -- Tên ngày trong tuần
    calendar_month VARCHAR(15),             -- Tên tháng
    calendar_year INT,                     -- Năm lịch
    calendar_year_month VARCHAR(255),       -- Định dạng YYYY-MMM
    day_of_month INT,                      -- Ngày trong tháng
    day_of_year INT,                       -- Ngày trong năm
    week_of_year_sunday INT,               -- Tuần của năm theo Chủ nhật
    year_week_sunday VARCHAR(255),          -- Định dạng YYYY-Www
    week_sunday_start DATE,                -- Ngày bắt đầu tuần theo Chủ nhật
    week_of_year_monday INT,               -- Tuần của năm theo Thứ hai
    year_week_monday VARCHAR(255),          -- Định dạng YYYY-Www
    week_monday_start DATE,                -- Ngày bắt đầu tuần theo Thứ hai
    quarter_of_year VARCHAR(255),                   -- Quý của năm
    quarter_since_2005 INT,                -- Quý kể từ năm 2005
    holiday VARCHAR(255),                   -- Trạng thái ngày lễ
    date_type VARCHAR(15)                  -- Kiểu ngày (Weekend/Weekday)
);

LOAD DATA INFILE "D:\\DataWarehouse (1)\\21130445_HuynhMinh\\data\\date_dim.csv"
INTO TABLE dim_dates
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n';

-- Table: dim_manufacturers
CREATE TABLE dim_manufacturers (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    natural_key INT UNSIGNED NOT NULL,
    manufacturer_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    is_active TINYINT(1) UNSIGNED DEFAULT '0' COMMENT '0: inactive, 1: active',
    delete_date INT,
    insert_date INT,
    update_date INT,
    
    FOREIGN KEY (delete_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (insert_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (update_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL
);

-- Table: dim_products
CREATE TABLE dim_products (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    natural_key INT UNSIGNED NOT NULL,
    sku_no VARCHAR(32) NOT NULL,
    product_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    product_description VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    image_url VARCHAR(255),
    specifications JSON DEFAULT NULL,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2) NOT NULL,
    stock INT UNSIGNED DEFAULT '0',
    manufacturer_id INT UNSIGNED,
    is_active TINYINT(1) UNSIGNED DEFAULT '0' COMMENT '0: inactive, 1: active',
    delete_date INT,
    insert_date INT,
    update_date INT,
    expired_date DATE DEFAULT '9999-12-31',
    INDEX idx_sku_no (sku_no),
    
    FOREIGN KEY (manufacturer_id) REFERENCES dim_manufacturers(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (delete_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (insert_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (update_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL
) COMMENT = 'SKU';

CREATE TABLE fact_sales (
		id INT AUTO_INCREMENT PRIMARY KEY,
		product_id INT UNSIGNED NOT NULL,
		quantity_sold INT UNSIGNED NOT NULL,
		total_revenue DECIMAL(15, 2) NOT NULL,
		total_cost DECIMAL(15, 2) NOT NULL,
		profit DECIMAL(15, 2) AS (total_revenue - total_cost) STORED,
		insert_date INT,
    update_date INT,
		
		FOREIGN KEY (insert_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (update_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (product_id) 	REFERENCES dim_products(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;