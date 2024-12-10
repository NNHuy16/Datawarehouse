-- Select the current database
SELECT DATABASE();

-- Drop the table if it already exists
DROP TABLE IF EXISTS dim_dates;

-- Create the dim_dates table
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

-- Load data from the CSV file into the dim_dates table
-- /var/lib/mysql-files/date_dim.csv
LOAD DATA INFILE "D:\Workspace\DataWarehouse\21130445_HuynhMinh\data\date_dim.csv"
INTO TABLE dim_dates
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n';
