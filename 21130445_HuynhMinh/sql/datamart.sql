CREATE DATABASE IF NOT EXISTS db_datamart;
USE db_datamart;

ALTER DATABASE db_datamart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP TABLE IF EXISTS aggregate_statistics;
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

CREATE TABLE aggregate_statistics (
    id INT AUTO_INCREMENT PRIMARY KEY,         -- ID duy nhất cho từng bản ghi
    manufacturer_name VARCHAR(255) NOT NULL,          -- Tên nhà sản xuất
    total_products_sold INT DEFAULT 0,           -- Tổng số sản phẩm đã bán
    total_revenue DECIMAL(15, 2) DEFAULT 0.0,    -- Tổng doanh thu
    total_profit DECIMAL(15, 2) DEFAULT 0.0,     -- Tổng lợi nhuận
    total_inventory INT DEFAULT 0,               -- Số lượng tồn kho
    best_selling_product VARCHAR(255),           -- Sản phẩm bán chạy nhất
    unsold_products INT DEFAULT 0,               -- Số lượng sản phẩm không bán được
    created_at INT, -- Ngày giờ tổng hợp dữ liệu
    updated_at INT, -- Ngày giờ cập nhật
		
    FOREIGN KEY (created_at) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (created_at) REFERENCES dim_dates(date_sk) ON DELETE SET NULL
);

USE db_datawarehouse;

DELIMITER $$

DROP PROCEDURE IF EXISTS InsertFactSales;
CREATE PROCEDURE InsertFactSales()
BEGIN
    DECLARE cur_date INT;
    
    -- Lấy date_sk từ bảng dim_dates dựa trên ngày hiện tại
    SELECT date_sk INTO cur_date
    FROM dim_dates
    WHERE full_date = CURDATE()
    LIMIT 1;

    -- Chèn 30 dòng dữ liệu vào bảng fact_sales
    INSERT INTO fact_sales (product_id, quantity_sold, total_revenue, total_cost, insert_date, update_date)
    VALUES
        (1, 50, 5000.00, 3500.00, cur_date, cur_date),
        (2, 30, 3000.00, 2100.00, cur_date, cur_date),
        (3, 20, 2000.00, 1500.00, cur_date, cur_date),
        (4, 40, 4000.00, 2800.00, cur_date, cur_date),
        (5, 25, 2500.00, 1900.00, cur_date, cur_date),
        (6, 60, 6000.00, 4200.00, cur_date, cur_date),
        (7, 15, 1500.00, 1100.00, cur_date, cur_date),
        (8, 35, 3500.00, 2450.00, cur_date, cur_date),
        (9, 45, 4500.00, 3150.00, cur_date, cur_date),
        (10, 55, 5500.00, 3850.00, cur_date, cur_date),
        (11, 20, 2000.00, 1400.00, cur_date, cur_date),
        (12, 25, 2500.00, 1750.00, cur_date, cur_date),
        (13, 30, 3000.00, 2100.00, cur_date, cur_date),
        (14, 50, 5000.00, 3500.00, cur_date, cur_date),
        (15, 60, 6000.00, 4200.00, cur_date, cur_date),
        (16, 10, 1000.00, 700.00, cur_date, cur_date),
        (17, 15, 1500.00, 1050.00, cur_date, cur_date),
        (18, 40, 4000.00, 2800.00, cur_date, cur_date),
        (19, 30, 3000.00, 2100.00, cur_date, cur_date),
        (20, 45, 4500.00, 3150.00, cur_date, cur_date),
        (21, 50, 5000.00, 3500.00, cur_date, cur_date),
        (22, 35, 3500.00, 2450.00, cur_date, cur_date),
        (23, 25, 2500.00, 1750.00, cur_date, cur_date),
				(24, 30, 3000.00, 2100.00, cur_date, cur_date),
        (25, 40, 4000.00, 2800.00, cur_date, cur_date),
        (26, 45, 4500.00, 3150.00, cur_date, cur_date),
        (27, 50, 5000.00, 3500.00, cur_date, cur_date),
        (28, 35, 3500.00, 2450.00, cur_date, cur_date),
        (29, 20, 2000.00, 1400.00, cur_date, cur_date),
        (30, 25, 2500.00, 1750.00, cur_date, cur_date),
				(52, 12, 2800.00, 1650.00, cur_date, cur_date),
				(153, 32, 3800.00, 2650.00, cur_date, cur_date),
				(214, 45, 4500.00, 3150.00, cur_date, cur_date),
				(261, 30, 1500.00, 1050.00, cur_date, cur_date),
				(284, 65, 3500.00, 2450.00, cur_date, cur_date),
				(333, 22, 3000.00, 2100.00, cur_date, cur_date),
				(359, 53, 2500.00, 1750.00, cur_date, cur_date);
END $$

DELIMITER ;

CALL InsertFactSales();

USE db_datamart;

DELIMITER $$

DROP PROCEDURE IF EXISTS load_to_datamart;
CREATE PROCEDURE load_to_datamart()
BEGIN
    DECLARE cur_date INT;
    DECLARE done INT DEFAULT 0;
    DECLARE manufacturer_name VARCHAR(255);
    DECLARE total_products_sold INT;
    DECLARE total_revenue DECIMAL(15, 2);
    DECLARE total_profit DECIMAL(15, 2);
    DECLARE total_inventory INT;
    DECLARE best_selling_product VARCHAR(255);
    DECLARE unsold_products INT;
    
    -- Declare the cursor for manufacturer statistics
    DECLARE manufacturer_cursor CURSOR FOR
    SELECT m.manufacturer_name, 
           SUM(fs.quantity_sold) AS total_products_sold,
           SUM(fs.total_revenue) AS total_revenue,
           SUM(fs.profit) AS total_profit,
           SUM(p.stock) AS total_inventory,
           -- Best-selling product per manufacturer
           (SELECT p.product_name 
            FROM db_datawarehouse.dim_products p
            JOIN db_datawarehouse.fact_sales fs ON p.id = fs.product_id
            WHERE fs.product_id = p.id
              AND p.manufacturer_id = m.id
            GROUP BY p.product_name
            ORDER BY SUM(fs.quantity_sold) DESC
            LIMIT 1) AS best_selling_product,
           SUM(CASE WHEN fs.quantity_sold = 0 THEN 1 ELSE 0 END) AS unsold_products
    FROM db_datawarehouse.dim_manufacturers m
    JOIN db_datawarehouse.dim_products p ON m.id = p.manufacturer_id
    LEFT JOIN db_datawarehouse.fact_sales fs ON p.id = fs.product_id
    GROUP BY m.id, m.manufacturer_name;  -- Add m.id to GROUP BY
    
    -- Declare a handler for the cursor to stop when done
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    
    -- Open the cursor
    OPEN manufacturer_cursor;
    
    -- Loop through the cursor and insert data into the aggregate_statistics table
    read_loop: LOOP
        FETCH manufacturer_cursor INTO manufacturer_name, total_products_sold, total_revenue, total_profit, total_inventory, best_selling_product, unsold_products;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Insert the aggregated statistics into the aggregate_statistics table
        INSERT INTO aggregate_statistics (manufacturer_name, total_products_sold, total_revenue, total_profit, total_inventory, best_selling_product, unsold_products, created_at, updated_at)
        VALUES (manufacturer_name, total_products_sold, total_revenue, total_profit, total_inventory, best_selling_product, unsold_products, cur_date, cur_date);
    END LOOP;
    
    -- Close the cursor
    CLOSE manufacturer_cursor;
END $$

DELIMITER ;

-- CALL load_to_datamart();