-- ==============================================================================
-- V7: Seed Budget & Mid-Range Laptops Under $1000
-- ==============================================================================

INSERT INTO products (id, sku, name, description, price, category, brand, status, stock_quantity, image_url, version)
VALUES 
('prod-lap-001', 'SKU-LAP-002', 'Acer Aspire 5 Slim Laptop 15.6" FHD', 'Budget-friendly everyday laptop with AMD Ryzen 5, 16GB RAM, 512GB SSD, WiFi 6, and backlit keyboard.', 499.99, 'Laptops', 'Acer', 'PUBLISHED', 35, 'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=600', 1),
('prod-lap-002', 'SKU-LAP-004', 'HP Pavilion 15 Touchscreen Laptop Core i5', 'Versatile lightweight laptop featuring 12th Gen Intel Core i5, 16GB DDR4, 512GB SSD, and fast charge support.', 649.99, 'Laptops', 'HP', 'PUBLISHED', 40, 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600', 1),
('prod-lap-003', 'SKU-LAP-005', 'ASUS Vivobook 16 Thin & Light Laptop', '16-inch WUXGA display laptop powered by AMD Ryzen 7, 16GB RAM, 1TB SSD, and ergonomic backlit chiclet keyboard.', 599.99, 'Laptops', 'ASUS', 'PUBLISHED', 50, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600', 1),
('prod-lap-004', 'SKU-LAP-006', 'Lenovo IdeaPad Slim 3 14" FHD Laptop', 'Affordable student and office productivity laptop with Intel Core i3, 8GB RAM, 256GB SSD, and Dolby Audio.', 429.99, 'Laptops', 'Lenovo', 'PUBLISHED', 65, 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=600', 1),
('prod-lap-005', 'SKU-LAP-007', 'Apple MacBook Air 13" M1 256GB', 'Ultra-thin Apple silicon laptop featuring 18-hour battery life, 8GB Unified Memory, and vibrant Retina display.', 799.00, 'Laptops', 'Apple', 'PUBLISHED', 20, 'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=600', 1),
('prod-lap-006', 'SKU-LAP-008', 'Dell Inspiron 15 3520 Laptop 120Hz', 'Reliable home & business laptop with Intel Core i5, 16GB RAM, 512GB NVMe SSD, and 120Hz anti-glare screen.', 529.99, 'Laptops', 'Dell', 'PUBLISHED', 30, 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=600', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), brand=VALUES(brand), stock_quantity=VALUES(stock_quantity);
