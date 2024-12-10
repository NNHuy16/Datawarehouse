USE db_datawarehouse;

DELIMITER $$

DROP PROCEDURE IF EXISTS insert_data_to_datawarehouse;
CREATE PROCEDURE insert_data_to_datawarehouse()
BEGIN
    -- Khai báo biến người dùng
    DECLARE cur_date INT;
    DECLARE manufacturers_inserted INT DEFAULT 0;
    DECLARE products_inserted INT DEFAULT 0;
    DECLARE total_inserted INT DEFAULT 0;
		 
    -- Gán giá trị cho biến cur_date
    SELECT date_sk INTO cur_date
    FROM dim_dates
    WHERE full_date = CURDATE()
    LIMIT 1;

    -- Thêm các Manufacturer mới vào
    INSERT INTO db_datawarehouse.dim_manufacturers (natural_key, manufacturer_name, is_active, insert_date, update_date) 
    SELECT DISTINCT sp.brand_id, sp.brand_name, 1, cur_date, cur_date
    FROM db_staging.staging_products AS sp
    WHERE NOT EXISTS (
        SELECT 1 FROM db_datawarehouse.dim_manufacturers dm
        WHERE dm.natural_key = sp.brand_id
    );
    -- Lấy số lượng bản ghi đã insert
    SET manufacturers_inserted = ROW_COUNT();

    -- Thêm các Product mới vào 
    INSERT INTO db_datawarehouse.dim_products (natural_key, sku_no, product_name, product_description, image_url, specifications, 
    price, original_price, stock, manufacturer_id, is_active, insert_date, update_date)
    SELECT sp.natural_key, sp.sku, sp.product_name, sp.short_description, sp.thumbnail_url, sp.specifications, sp.price, sp.original_price, 
    sp.stock_item_qty, dm.id, 1, cur_date, cur_date
    FROM db_staging.staging_products AS sp
    JOIN db_datawarehouse.dim_manufacturers AS dm ON dm.manufacturer_name = sp.brand_name
    WHERE NOT EXISTS (
        SELECT 1 FROM db_datawarehouse.dim_products AS dp
        WHERE dp.natural_key = sp.natural_key
    );
    -- Lấy số lượng bản ghi đã insert
    SET products_inserted = ROW_COUNT();
    
    SET SQL_SAFE_UPDATES = 0;
    
    -- Cập nhật các sản phẩm cũ (đánh dấu hết hiệu lực nếu có sự thay đổi)
    UPDATE db_datawarehouse.dim_products AS dp
    SET dp.is_active = 0, dp.expired_date = CURRENT_DATE, dp.update_date = cur_date
    WHERE dp.expired_date = '9999-12-31' 
    AND dp.is_active = 1
    AND EXISTS (
        SELECT 1 FROM db_staging.staging_products AS sp
        WHERE dp.natural_key = sp.natural_key
        AND (dp.sku_no <> sp.sku
            OR dp.product_name <> sp.product_name
            OR dp.product_description <> sp.short_description
            OR dp.image_url <> sp.thumbnail_url
            OR dp.specifications <> sp.specifications
            OR dp.price <> sp.price
            OR dp.original_price <> sp.original_price
            OR dp.stock <> sp.stock_item_qty)
    );
    
    SET SQL_SAFE_UPDATES = 1;

    -- Thêm bản ghi mới với các thay đổi vào dim_products (SCD Type 2)
    INSERT INTO db_datawarehouse.dim_products (natural_key, sku_no, product_name, product_description, image_url, specifications, 
    price, original_price, stock, manufacturer_id, is_active, insert_date, update_date, expired_date)
    SELECT sp.natural_key, sp.sku, sp.product_name, sp.short_description, sp.thumbnail_url, sp.specifications, sp.price, sp.original_price, 
    sp.stock_item_qty, dm.id, 1, cur_date, cur_date, '9999-12-31'
    FROM db_staging.staging_products AS sp
    JOIN db_datawarehouse.dim_manufacturers AS dm ON dm.manufacturer_name = sp.brand_name
    WHERE EXISTS (
        SELECT 1 FROM db_datawarehouse.dim_products AS dp
        WHERE dp.natural_key = sp.natural_key
        AND dp.is_active = 0 -- Đảm bảo chỉ chèn bản ghi thay đổi khi sản phẩm cũ bị đánh dấu không còn hiệu lực
        AND (dp.sku_no <> sp.sku
            OR dp.product_name <> sp.product_name
            OR dp.product_description <> sp.short_description
            OR dp.image_url <> sp.thumbnail_url
            OR dp.specifications <> sp.specifications
            OR dp.price <> sp.price
            OR dp.original_price <> sp.original_price
            OR dp.stock <> sp.stock_item_qty)
    );

    SET products_inserted = products_inserted + ROW_COUNT();
    -- Tính tổng số bản ghi đã insert
    SET total_inserted = manufacturers_inserted + products_inserted;
		
		SELECT total_inserted;

END $$

DELIMITER ;

-- CALL insert_data_to_datawarehouse();