-- ==============================================================================
-- V2: Expanded Product Catalog with Real High-Resolution Images
-- ==============================================================================

-- Update existing items to point to local high-res product images
UPDATE products SET 
    image_url = '/images/products/SKU-LAP-001.jpg',
    price = 2499.99,
    name = 'Apple MacBook Pro 16" M3 Max'
WHERE id = 'prod-001' OR sku = 'SKU-LAP-001';

UPDATE products SET 
    image_url = '/images/products/SKU-PHN-002.jpg',
    name = 'Samsung Galaxy S24 Ultra 5G'
WHERE id = 'prod-002' OR sku = 'SKU-PHN-002';

UPDATE products SET 
    image_url = '/images/products/SKU-AUD-001.jpg',
    name = 'Sony WH-1000XM5 Wireless Headphones'
WHERE id = 'prod-003' OR sku = 'SKU-AUD-003';

UPDATE products SET 
    image_url = '/images/products/SKU-WCH-001.jpg',
    name = 'Apple Watch Ultra 2 Titanium'
WHERE id = 'prod-004' OR sku = 'SKU-WCH-004';

UPDATE products SET 
    image_url = '/images/products/SKU-GAM-004.jpg',
    name = 'Keychron Q1 Pro Mechanical Keyboard'
WHERE id = 'prod-005' OR sku = 'SKU-DSK-005';

-- Insert full catalog of 20 products
INSERT INTO products (id, sku, name, description, price, category, status, stock_quantity, image_url, version)
VALUES 
('prod-002-dell', 'SKU-LAP-002', 'Dell XPS 15 OLED InfinityEdge', '15.6" 3.5K OLED touch display, Intel Core i9, 32GB DDR5, 1TB NVMe SSD, NVIDIA RTX 4070.', 1899.00, 'Electronics', 'PUBLISHED', 30, '/images/products/SKU-LAP-002.jpg', 1),
('prod-003-lenovo', 'SKU-LAP-003', 'Lenovo ThinkPad X1 Carbon Gen 12', 'Ultralight enterprise business laptop with carbon fiber chassis, Intel Core Ultra 7, 32GB RAM.', 1649.00, 'Electronics', 'PUBLISHED', 25, '/images/products/SKU-LAP-003.jpg', 1),
('prod-004-iphone', 'SKU-PHN-001', 'Apple iPhone 16 Pro Max 256GB', 'Grade 5 Titanium design with A18 Pro chip, 48MP Fusion camera, and Camera Control.', 1199.00, 'Mobile', 'PUBLISHED', 80, '/images/products/SKU-PHN-001.jpg', 1),
('prod-006-pixel', 'SKU-PHN-003', 'Google Pixel 9 Pro Fold', 'Revolutionary foldable phone with Tensor G4, Gemini AI integrated, and 8-inch Super Actua Flex display.', 1799.00, 'Mobile', 'PUBLISHED', 40, '/images/products/SKU-PHN-003.jpg', 1),
('prod-008-bose', 'SKU-AUD-002', 'Bose QuietComfort Ultra Spatial Audio', 'World-class active noise cancellation with breakthrough Bose Immersive Audio and CustomTune.', 429.00, 'Audio', 'PUBLISHED', 85, '/images/products/SKU-AUD-002.jpg', 1),
('prod-009-airpods', 'SKU-AUD-003', 'Apple AirPods Pro 2 USB-C', 'Up to 2x more Active Noise Cancellation, Adaptive Audio, Transparency mode, and MagSafe USB-C Case.', 249.00, 'Audio', 'PUBLISHED', 150, '/images/products/SKU-AUD-003.jpg', 1),
('prod-011-garmin', 'SKU-WCH-002', 'Garmin Fenix 7X Sapphire Solar', 'Multisport GPS smartwatch with solar charging lens, built-in LED flashlight, and TopoActive maps.', 899.99, 'Wearables', 'PUBLISHED', 35, '/images/products/SKU-WCH-002.jpg', 1),
('prod-012-galaxy-watch', 'SKU-WCH-003', 'Samsung Galaxy Watch 6 Classic', 'Iconic rotating bezel, Sapphire Crystal glass, advanced sleep coaching, and BIA body composition.', 349.00, 'Wearables', 'PUBLISHED', 75, '/images/products/SKU-WCH-003.jpg', 1),
('prod-013-ps5', 'SKU-GAM-001', 'Sony PlayStation 5 Slim Digital', 'Experience lightning-fast loading with an ultra-high speed SSD, deeper immersion with haptic feedback.', 449.99, 'Gaming', 'PUBLISHED', 90, '/images/products/SKU-GAM-001.jpg', 1),
('prod-014-xbox', 'SKU-GAM-002', 'Xbox Series X 1TB Console', 'The fastest, most powerful Xbox ever. 12 teraflops of raw graphic processing power and 4K gaming.', 499.00, 'Gaming', 'PUBLISHED', 70, '/images/products/SKU-GAM-002.jpg', 1),
('prod-015-logitech-mouse', 'SKU-GAM-003', 'Logitech G Pro X Superlight 2 Mouse', 'Lightweight 60g esports mouse with HERO 2 sensor, LIGHTSPEED wireless, and hybrid optical-mechanical switches.', 159.99, 'Accessories', 'PUBLISHED', 140, '/images/products/SKU-GAM-003.jpg', 1),
('prod-017-sony-cam', 'SKU-CAM-001', 'Sony Alpha a7 IV Full-Frame Camera', '33MP full-frame Exmor R back-illuminated CMOS sensor with BIONZ XR processing engine and 4K 60p video.', 2498.00, 'Cameras', 'PUBLISHED', 20, '/images/products/SKU-CAM-001.jpg', 1),
('prod-018-fuji-cam', 'SKU-CAM-002', 'Fujifilm X-T5 Mirrorless Camera', '40.2MP X-Trans CMOS 5 HR sensor, 5-axis in-body image stabilization, and classic dial-based operation.', 1699.00, 'Cameras', 'PUBLISHED', 28, '/images/products/SKU-CAM-002.jpg', 1),
('prod-019-dyson', 'SKU-HOM-001', 'Dyson V15 Detect Cordless Vacuum', 'Laser reveals microscopic dust, intelligently optimizes suction and run time, with Piezo sensor dust count.', 749.99, 'Home', 'PUBLISHED', 50, '/images/products/SKU-HOM-001.jpg', 1),
('prod-020-breville', 'SKU-HOM-002', 'Breville Barista Touch Espresso Machine', 'Automated touchscreen coffee machine with integrated grinder, ThermoJet 3-second heat up and microfoam milk.', 999.95, 'Home', 'PUBLISHED', 35, '/images/products/SKU-HOM-002.jpg', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), image_url=VALUES(image_url), stock_quantity=VALUES(stock_quantity);
