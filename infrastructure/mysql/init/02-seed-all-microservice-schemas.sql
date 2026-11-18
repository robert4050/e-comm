-- ==============================================================================
-- e-comm Master Database Schema & Realistic Seed Data for All Microservices
-- ==============================================================================

-- -----------------------------------------------------------------------------
-- 1. category_db: Categories, Brands, and Dynamic Attributes
-- -----------------------------------------------------------------------------
USE `category_db`;

CREATE TABLE IF NOT EXISTS `categories` (
    `id` VARCHAR(36) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `slug` VARCHAR(100) NOT NULL UNIQUE,
    `description` TEXT,
    `parent_id` VARCHAR(36) NULL,
    `icon` VARCHAR(64) DEFAULT 'folder',
    `display_order` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `brands` (
    `id` VARCHAR(36) PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `slug` VARCHAR(100) NOT NULL UNIQUE,
    `origin_country` VARCHAR(64),
    `website_url` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `category_attributes` (
    `id` VARCHAR(36) PRIMARY KEY,
    `category_id` VARCHAR(36) NOT NULL,
    `attribute_name` VARCHAR(64) NOT NULL,
    `data_type` VARCHAR(32) NOT NULL,
    `is_filterable` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `categories` (`id`, `name`, `slug`, `description`, `icon`, `display_order`) VALUES
('cat-01', 'Electronics & Computers', 'electronics', 'High-performance laptops, workstations and computing accessories', 'laptop', 1),
('cat-02', 'Mobile & Smartphones', 'mobile', 'Next-generation flagship smartphones and foldable devices', 'smartphone', 2),
('cat-03', 'Audio & Sound', 'audio', 'Studio headphones, noise-canceling earbuds, and spatial audio', 'headphones', 3),
('cat-04', 'Wearables & Smartwatches', 'wearables', 'Fitness trackers, titanium sports watches and smart accessories', 'watch', 4),
('cat-05', 'Gaming & Consoles', 'gaming', 'Next-gen consoles, esports mice, and custom mechanical keyboards', 'sports_esports', 5),
('cat-06', 'Cameras & Optics', 'cameras', 'Full-frame mirrorless cameras, lenses, and photography gear', 'photo_camera', 6),
('cat-07', 'Smart Home & Appliances', 'home', 'Cordless laser vacuums, touch espresso machines and smart living', 'home', 7)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `brands` (`id`, `name`, `slug`, `origin_country`, `website_url`) VALUES
('br-01', 'Apple', 'apple', 'USA', 'https://www.apple.com'),
('br-02', 'Dell', 'dell', 'USA', 'https://www.dell.com'),
('br-03', 'Lenovo', 'lenovo', 'China', 'https://www.lenovo.com'),
('br-04', 'Samsung', 'samsung', 'South Korea', 'https://www.samsung.com'),
('br-05', 'Google', 'google', 'USA', 'https://store.google.com'),
('br-06', 'Sony', 'sony', 'Japan', 'https://www.sony.com'),
('br-07', 'Bose', 'bose', 'USA', 'https://www.bose.com'),
('br-08', 'Garmin', 'garmin', 'USA', 'https://www.garmin.com'),
('br-09', 'Logitech', 'logitech', 'Switzerland', 'https://www.logitech.com'),
('br-10', 'Keychron', 'keychron', 'France', 'https://www.keychron.com'),
('br-11', 'Dyson', 'dyson', 'UK', 'https://www.dyson.com'),
('br-12', 'Breville', 'breville', 'Australia', 'https://www.breville.com')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- -----------------------------------------------------------------------------
-- 2. inventory_db: Warehouses, Stocks, Reservations and Movements
-- -----------------------------------------------------------------------------
USE `inventory_db`;

CREATE TABLE IF NOT EXISTS `warehouses` (
    `id` VARCHAR(36) PRIMARY KEY,
    `code` VARCHAR(32) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `country` VARCHAR(64) NOT NULL,
    `city` VARCHAR(64) NOT NULL,
    `address` VARCHAR(255),
    `capacity_units` INT DEFAULT 50000
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `inventory_items` (
    `id` VARCHAR(36) PRIMARY KEY,
    `sku` VARCHAR(64) NOT NULL,
    `warehouse_id` VARCHAR(36) NOT NULL,
    `available_quantity` INT NOT NULL DEFAULT 0,
    `reserved_quantity` INT NOT NULL DEFAULT 0,
    `safety_stock` INT NOT NULL DEFAULT 5,
    `reorder_point` INT NOT NULL DEFAULT 15,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_sku_warehouse` (`sku`, `warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `stock_reservations` (
    `id` VARCHAR(36) PRIMARY KEY,
    `reservation_token` VARCHAR(64) NOT NULL UNIQUE,
    `sku` VARCHAR(64) NOT NULL,
    `quantity` INT NOT NULL,
    `status` VARCHAR(32) DEFAULT 'ACTIVE',
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `stock_movements` (
    `id` VARCHAR(36) PRIMARY KEY,
    `sku` VARCHAR(64) NOT NULL,
    `movement_type` VARCHAR(32) NOT NULL,
    `quantity` INT NOT NULL,
    `reference_id` VARCHAR(64),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `warehouses` (`id`, `code`, `name`, `country`, `city`, `address`) VALUES
('wh-01', 'WH-US-EAST', 'East Coast Distribution Center', 'USA', 'Newark, NJ', '100 Distribution Way'),
('wh-02', 'WH-US-WEST', 'West Coast Logistics Hub', 'USA', 'Oakland, CA', '500 Maritime Blvd'),
('wh-03', 'WH-EU-CENTRAL', 'European Central Fulfillment', 'Germany', 'Frankfurt', 'Logistikpark 12')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `inventory_items` (`id`, `sku`, `warehouse_id`, `available_quantity`, `reserved_quantity`, `safety_stock`, `reorder_point`) VALUES
('inv-01', 'SKU-LAP-001', 'wh-01', 30, 2, 5, 10),
('inv-02', 'SKU-LAP-001', 'wh-02', 15, 0, 5, 10),
('inv-03', 'SKU-LAP-002', 'wh-01', 20, 1, 5, 10),
('inv-04', 'SKU-LAP-003', 'wh-01', 18, 0, 5, 10),
('inv-05', 'SKU-PHN-001', 'wh-01', 50, 4, 10, 20),
('inv-06', 'SKU-PHN-001', 'wh-02', 30, 0, 10, 20),
('inv-07', 'SKU-PHN-002', 'wh-01', 45, 2, 10, 15),
('inv-08', 'SKU-PHN-003', 'wh-01', 25, 1, 5, 10),
('inv-09', 'SKU-AUD-001', 'wh-01', 80, 5, 15, 25),
('inv-10', 'SKU-AUD-002', 'wh-01', 55, 3, 10, 20),
('inv-11', 'SKU-AUD-003', 'wh-01', 100, 8, 20, 30),
('inv-12', 'SKU-WCH-001', 'wh-01', 40, 2, 10, 15),
('inv-13', 'SKU-WCH-002', 'wh-01', 25, 0, 5, 10),
('inv-14', 'SKU-WCH-003', 'wh-01', 50, 1, 10, 15),
('inv-15', 'SKU-GAM-001', 'wh-01', 60, 4, 15, 20),
('inv-16', 'SKU-GAM-002', 'wh-01', 50, 2, 10, 15),
('inv-17', 'SKU-GAM-003', 'wh-01', 95, 3, 15, 25),
('inv-18', 'SKU-GAM-004', 'wh-01', 75, 2, 15, 20),
('inv-19', 'SKU-CAM-001', 'wh-01', 15, 0, 3, 5),
('inv-20', 'SKU-CAM-002', 'wh-01', 20, 1, 5, 8),
('inv-21', 'SKU-HOM-001', 'wh-01', 35, 2, 8, 12),
('inv-22', 'SKU-HOM-002', 'wh-01', 25, 1, 5, 10)
ON DUPLICATE KEY UPDATE available_quantity=VALUES(available_quantity);

-- -----------------------------------------------------------------------------
-- 3. customer_db: Profiles, Membership Tiers, and Preferences
-- -----------------------------------------------------------------------------
USE `customer_db`;

CREATE TABLE IF NOT EXISTS `customers` (
    `id` VARCHAR(36) PRIMARY KEY,
    `keycloak_id` VARCHAR(64) UNIQUE,
    `email` VARCHAR(128) NOT NULL UNIQUE,
    `first_name` VARCHAR(64) NOT NULL,
    `last_name` VARCHAR(64) NOT NULL,
    `phone_number` VARCHAR(32),
    `membership_tier` VARCHAR(32) DEFAULT 'STANDARD',
    `reward_points` INT DEFAULT 100,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `customer_preferences` (
    `customer_id` VARCHAR(36) PRIMARY KEY,
    `preferred_currency` VARCHAR(8) DEFAULT 'USD',
    `locale` VARCHAR(16) DEFAULT 'en-US',
    `newsletter_opt_in` BOOLEAN DEFAULT TRUE,
    `sms_notifications` BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `customers` (`id`, `keycloak_id`, `email`, `first_name`, `last_name`, `phone_number`, `membership_tier`, `reward_points`) VALUES
('cust-01', 'kc-usr-003', 'customer@e-comm.local', 'John', 'Customer', '+1-555-0199', 'GOLD', 1250),
('cust-02', 'kc-usr-002', 'manager@e-comm.local', 'Sarah', 'Manager', '+1-555-0144', 'PLATINUM', 3400),
('cust-03', 'kc-usr-001', 'admin@e-comm.local', 'System', 'Administrator', '+1-555-0100', 'VIP', 5000),
('cust-04', 'kc-usr-004', 'alice.smith@example.com', 'Alice', 'Smith', '+1-555-0211', 'SILVER', 450),
('cust-05', 'kc-usr-005', 'bob.jones@example.com', 'Bob', 'Jones', '+1-555-0322', 'STANDARD', 100)
ON DUPLICATE KEY UPDATE first_name=VALUES(first_name);

INSERT INTO `customer_preferences` (`customer_id`, `preferred_currency`, `locale`, `newsletter_opt_in`, `sms_notifications`) VALUES
('cust-01', 'USD', 'en-US', TRUE, TRUE),
('cust-02', 'USD', 'en-US', TRUE, FALSE),
('cust-03', 'USD', 'en-US', FALSE, FALSE),
('cust-04', 'EUR', 'de-DE', TRUE, TRUE),
('cust-05', 'GBP', 'en-GB', FALSE, FALSE)
ON DUPLICATE KEY UPDATE preferred_currency=VALUES(preferred_currency);

-- -----------------------------------------------------------------------------
-- 4. address_db: Shipping and Billing Addresses, Delivery Zones
-- -----------------------------------------------------------------------------
USE `address_db`;

CREATE TABLE IF NOT EXISTS `addresses` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NOT NULL,
    `address_type` VARCHAR(32) NOT NULL,
    `recipient_name` VARCHAR(100) NOT NULL,
    `street_line1` VARCHAR(255) NOT NULL,
    `street_line2` VARCHAR(255),
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(64) NOT NULL,
    `postal_code` VARCHAR(32) NOT NULL,
    `country` VARCHAR(64) NOT NULL,
    `is_default` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `delivery_zones` (
    `id` VARCHAR(36) PRIMARY KEY,
    `zone_name` VARCHAR(64) NOT NULL,
    `country_code` VARCHAR(8) NOT NULL,
    `standard_days` INT NOT NULL,
    `express_days` INT NOT NULL,
    `base_rate` DECIMAL(8,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `addresses` (`id`, `customer_id`, `address_type`, `recipient_name`, `street_line1`, `street_line2`, `city`, `state`, `postal_code`, `country`, `is_default`) VALUES
('addr-01', 'cust-01', 'SHIPPING', 'John Customer', '742 Evergreen Terrace', 'Apt 4B', 'Springfield', 'OR', '97477', 'USA', TRUE),
('addr-02', 'cust-01', 'BILLING', 'John Customer', '742 Evergreen Terrace', 'Apt 4B', 'Springfield', 'OR', '97477', 'USA', TRUE),
('addr-03', 'cust-02', 'SHIPPING', 'Sarah Manager', '350 5th Ave', 'Floor 22', 'New York', 'NY', '10118', 'USA', TRUE),
('addr-04', 'cust-04', 'SHIPPING', 'Alice Smith', 'Kaufingerstrasse 15', NULL, 'Munich', 'Bavaria', '80331', 'Germany', TRUE)
ON DUPLICATE KEY UPDATE recipient_name=VALUES(recipient_name);

INSERT INTO `delivery_zones` (`id`, `zone_name`, `country_code`, `standard_days`, `express_days`, `base_rate`) VALUES
('zone-01', 'US Continental', 'US', 3, 1, 9.99),
('zone-02', 'US Alaska/Hawaii', 'US', 5, 2, 19.99),
('zone-03', 'Western Europe', 'EU', 4, 2, 14.99),
('zone-04', 'United Kingdom', 'GB', 4, 2, 12.99)
ON DUPLICATE KEY UPDATE zone_name=VALUES(zone_name);

-- -----------------------------------------------------------------------------
-- 5. pricing_db: Price Rules, Coupons, and Promotions
-- -----------------------------------------------------------------------------
USE `pricing_db`;

CREATE TABLE IF NOT EXISTS `pricing_rules` (
    `id` VARCHAR(36) PRIMARY KEY,
    `sku` VARCHAR(64) NOT NULL UNIQUE,
    `base_price` DECIMAL(10,2) NOT NULL,
    `sale_price` DECIMAL(10,2),
    `currency` VARCHAR(8) DEFAULT 'USD',
    `is_active` BOOLEAN DEFAULT TRUE,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `coupons` (
    `id` VARCHAR(36) PRIMARY KEY,
    `code` VARCHAR(32) NOT NULL UNIQUE,
    `description` VARCHAR(255),
    `discount_type` VARCHAR(16) NOT NULL,
    `discount_value` DECIMAL(8,2) NOT NULL,
    `min_order_amount` DECIMAL(10,2) DEFAULT 0.00,
    `usage_limit` INT DEFAULT 1000,
    `usage_count` INT DEFAULT 0,
    `is_active` BOOLEAN DEFAULT TRUE,
    `expires_at` TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `pricing_rules` (`id`, `sku`, `base_price`, `sale_price`) VALUES
('pr-01', 'SKU-LAP-001', 2499.99, 2399.99),
('pr-02', 'SKU-LAP-002', 1899.00, 1799.00),
('pr-03', 'SKU-LAP-003', 1649.00, NULL),
('pr-04', 'SKU-PHN-001', 1199.00, NULL),
('pr-05', 'SKU-PHN-002', 1299.99, 1199.99),
('pr-06', 'SKU-AUD-001', 398.00, 348.00),
('pr-07', 'SKU-AUD-002', 429.00, NULL),
('pr-08', 'SKU-WCH-001', 799.00, 749.00)
ON DUPLICATE KEY UPDATE base_price=VALUES(base_price);

INSERT INTO `coupons` (`id`, `code`, `description`, `discount_type`, `discount_value`, `min_order_amount`, `usage_limit`, `usage_count`, `expires_at`) VALUES
('cp-01', 'WELCOME10', '10% off for new customers', 'PERCENT', 10.00, 50.00, 5000, 142, '2027-12-31 23:59:59'),
('cp-02', 'SAVE50', '$50 off on orders over $300', 'FIXED', 50.00, 300.00, 1000, 89, '2027-12-31 23:59:59'),
('cp-03', 'BLACKFRIDAY', 'Black Friday Special 20% off', 'PERCENT', 20.00, 100.00, 2000, 412, '2027-12-31 23:59:59'),
('cp-04', 'FREESHIP', 'Free express shipping on all orders', 'FIXED', 9.99, 25.00, 10000, 1280, '2027-12-31 23:59:59')
ON DUPLICATE KEY UPDATE discount_value=VALUES(discount_value);

-- -----------------------------------------------------------------------------
-- 6. cart_db: Persistent Shopping Carts and Cart Items
-- -----------------------------------------------------------------------------
USE `cart_db`;

CREATE TABLE IF NOT EXISTS `carts` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NULL,
    `session_id` VARCHAR(64) NULL,
    `status` VARCHAR(32) DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `cart_items` (
    `id` VARCHAR(36) PRIMARY KEY,
    `cart_id` VARCHAR(36) NOT NULL,
    `sku` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `image_url` VARCHAR(512),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `carts` (`id`, `customer_id`, `status`) VALUES
('cart-01', 'cust-01', 'ACTIVE'),
('cart-02', 'cust-04', 'ACTIVE')
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO `cart_items` (`id`, `cart_id`, `sku`, `product_name`, `unit_price`, `quantity`, `image_url`) VALUES
('ci-01', 'cart-01', 'SKU-LAP-001', 'Apple MacBook Pro 16" M3 Max', 2499.99, 1, '/images/products/SKU-LAP-001.jpg'),
('ci-02', 'cart-01', 'SKU-AUD-001', 'Sony WH-1000XM5 Wireless Headphones', 398.00, 1, '/images/products/SKU-AUD-001.jpg'),
('ci-03', 'cart-02', 'SKU-PHN-001', 'Apple iPhone 16 Pro Max 256GB', 1199.00, 1, '/images/products/SKU-PHN-001.jpg')
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- -----------------------------------------------------------------------------
-- 7. order_db: Orders, Order Items, Status History, and Outbox Events
-- -----------------------------------------------------------------------------
USE `order_db`;

CREATE TABLE IF NOT EXISTS `orders` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_number` VARCHAR(32) NOT NULL UNIQUE,
    `customer_id` VARCHAR(36) NOT NULL,
    `customer_email` VARCHAR(128) NOT NULL,
    `subtotal` DECIMAL(10,2) NOT NULL,
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00,
    `tax_amount` DECIMAL(10,2) DEFAULT 0.00,
    `shipping_fee` DECIMAL(10,2) DEFAULT 0.00,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `currency` VARCHAR(8) DEFAULT 'USD',
    `status` VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED',
    `payment_status` VARCHAR(32) NOT NULL DEFAULT 'CAPTURED',
    `shipping_status` VARCHAR(32) NOT NULL DEFAULT 'PROCESSING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_items` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_id` VARCHAR(36) NOT NULL,
    `sku` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `quantity` INT NOT NULL,
    `subtotal` DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_addresses` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_id` VARCHAR(36) NOT NULL,
    `address_type` VARCHAR(16) NOT NULL,
    `recipient_name` VARCHAR(100) NOT NULL,
    `street` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `state` VARCHAR(64) NOT NULL,
    `postal_code` VARCHAR(32) NOT NULL,
    `country` VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_status_history` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_id` VARCHAR(36) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `comment` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `outbox_events` (
    `id` VARCHAR(36) PRIMARY KEY,
    `aggregate_type` VARCHAR(64) NOT NULL,
    `aggregate_id` VARCHAR(64) NOT NULL,
    `event_type` VARCHAR(128) NOT NULL,
    `payload` LONGTEXT NOT NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `processed_at` TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `orders` (`id`, `order_number`, `customer_id`, `customer_email`, `subtotal`, `discount_amount`, `tax_amount`, `shipping_fee`, `total_amount`, `status`, `payment_status`, `shipping_status`) VALUES
('ord-01', 'ORD-2026-1001', 'cust-01', 'customer@e-comm.local', 2897.99, 50.00, 227.84, 0.00, 3075.83, 'DELIVERED', 'CAPTURED', 'DELIVERED'),
('ord-02', 'ORD-2026-1002', 'cust-01', 'customer@e-comm.local', 1199.00, 0.00, 95.92, 9.99, 1304.91, 'SHIPPED', 'CAPTURED', 'IN_TRANSIT'),
('ord-03', 'ORD-2026-1003', 'cust-02', 'manager@e-comm.local', 449.99, 10.00, 35.20, 0.00, 475.19, 'CONFIRMED', 'CAPTURED', 'PROCESSING'),
('ord-04', 'ORD-2026-1004', 'cust-04', 'alice.smith@example.com', 799.00, 0.00, 63.92, 9.99, 872.91, 'CONFIRMED', 'CAPTURED', 'PROCESSING')
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount);

INSERT INTO `order_items` (`id`, `order_id`, `sku`, `product_name`, `unit_price`, `quantity`, `subtotal`) VALUES
('oi-01', 'ord-01', 'SKU-LAP-001', 'Apple MacBook Pro 16" M3 Max', 2499.99, 1, 2499.99),
('oi-02', 'ord-01', 'SKU-AUD-001', 'Sony WH-1000XM5 Wireless Headphones', 398.00, 1, 398.00),
('oi-03', 'ord-02', 'SKU-PHN-001', 'Apple iPhone 16 Pro Max 256GB', 1199.00, 1, 1199.00),
('oi-04', 'ord-03', 'SKU-GAM-001', 'Sony PlayStation 5 Slim Digital', 449.99, 1, 449.99),
('oi-05', 'ord-04', 'SKU-WCH-001', 'Apple Watch Ultra 2 Titanium', 799.00, 1, 799.00)
ON DUPLICATE KEY UPDATE subtotal=VALUES(subtotal);

INSERT INTO `order_addresses` (`id`, `order_id`, `address_type`, `recipient_name`, `street`, `city`, `state`, `postal_code`, `country`) VALUES
('oa-01', 'ord-01', 'SHIPPING', 'John Customer', '742 Evergreen Terrace', 'Springfield', 'OR', '97477', 'USA'),
('oa-02', 'ord-02', 'SHIPPING', 'John Customer', '742 Evergreen Terrace', 'Springfield', 'OR', '97477', 'USA'),
('oa-03', 'ord-03', 'SHIPPING', 'Sarah Manager', '350 5th Ave', 'New York', 'NY', '10118', 'USA')
ON DUPLICATE KEY UPDATE street=VALUES(street);

INSERT INTO `order_status_history` (`id`, `order_id`, `status`, `comment`) VALUES
('osh-01', 'ord-01', 'PENDING_PAYMENT', 'Order initialized by checkout saga'),
('osh-02', 'ord-01', 'CONFIRMED', 'Payment authorized and stock reserved'),
('osh-03', 'ord-01', 'SHIPPED', 'Dispatched via DHL Express tracking DHL-9847291'),
('osh-04', 'ord-01', 'DELIVERED', 'Delivered at front door with customer signature'),
('osh-05', 'ord-02', 'CONFIRMED', 'Payment verified'),
('osh-06', 'ord-02', 'SHIPPED', 'In transit via FedEx Express FX-4820194')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- -----------------------------------------------------------------------------
-- 8. checkout_db: Checkout Sessions & Distributed Saga Orchestration
-- -----------------------------------------------------------------------------
USE `checkout_db`;

CREATE TABLE IF NOT EXISTS `checkout_sessions` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NOT NULL,
    `cart_id` VARCHAR(36) NOT NULL,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `saga_state` VARCHAR(32) NOT NULL,
    `idempotency_key` VARCHAR(64) NOT NULL UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `saga_steps` (
    `id` VARCHAR(36) PRIMARY KEY,
    `checkout_id` VARCHAR(36) NOT NULL,
    `step_name` VARCHAR(64) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `payload` TEXT,
    `executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `checkout_sessions` (`id`, `customer_id`, `cart_id`, `total_amount`, `saga_state`, `idempotency_key`) VALUES
('chk-01', 'cust-01', 'cart-01', 3075.83, 'ORDER_CONFIRMED', 'idem-key-884920-1'),
('chk-02', 'cust-01', 'cart-01', 1304.91, 'ORDER_CONFIRMED', 'idem-key-884920-2')
ON DUPLICATE KEY UPDATE saga_state=VALUES(saga_state);

INSERT INTO `saga_steps` (`id`, `checkout_id`, `step_name`, `status`, `payload`) VALUES
('step-01', 'chk-01', 'PRICE_VALIDATED', 'SUCCESS', '{"quote": 2897.99, "coupon": "SAVE50"}'),
('step-02', 'chk-01', 'INVENTORY_RESERVED', 'SUCCESS', '{"reservations": ["SKU-LAP-001", "SKU-AUD-001"]}'),
('step-03', 'chk-01', 'PAYMENT_AUTHORIZED', 'SUCCESS', '{"transactionId": "txn-99482", "amount": 3075.83}'),
('step-04', 'chk-01', 'ORDER_CONFIRMED', 'SUCCESS', '{"orderNumber": "ORD-2026-1001"}')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- -----------------------------------------------------------------------------
-- 9. payment_db: Payment Transactions, Webhooks, Saved Cards
-- -----------------------------------------------------------------------------
USE `payment_db`;

CREATE TABLE IF NOT EXISTS `payment_transactions` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_id` VARCHAR(36) NOT NULL,
    `transaction_reference` VARCHAR(64) NOT NULL UNIQUE,
    `provider` VARCHAR(32) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `currency` VARCHAR(8) DEFAULT 'USD',
    `status` VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    `payment_method` VARCHAR(32) NOT NULL,
    `card_last4` VARCHAR(4),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `payment_methods` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NOT NULL,
    `provider` VARCHAR(32) NOT NULL,
    `card_brand` VARCHAR(32),
    `last4` VARCHAR(4) NOT NULL,
    `exp_month` INT NOT NULL,
    `exp_year` INT NOT NULL,
    `is_default` BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `payment_transactions` (`id`, `order_id`, `transaction_reference`, `provider`, `amount`, `status`, `payment_method`, `card_last4`) VALUES
('pay-01', 'ord-01', 'ch_3N4kLs2eZvKYlo2C1', 'STRIPE', 3075.83, 'SUCCESS', 'CREDIT_CARD', '4242'),
('pay-02', 'ord-02', 'ch_3N4kLs2eZvKYlo2C2', 'STRIPE', 1304.91, 'SUCCESS', 'APPLE_PAY', '8812'),
('pay-03', 'ord-03', 'PAYID-MT74921-USD', 'PAYPAL', 475.19, 'SUCCESS', 'PAYPAL', NULL)
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO `payment_methods` (`id`, `customer_id`, `provider`, `card_brand`, `last4`, `exp_month`, `exp_year`, `is_default`) VALUES
('pm-01', 'cust-01', 'STRIPE', 'Visa', '4242', 12, 2028, TRUE),
('pm-02', 'cust-01', 'STRIPE', 'Mastercard', '8812', 8, 2027, FALSE)
ON DUPLICATE KEY UPDATE last4=VALUES(last4);

-- -----------------------------------------------------------------------------
-- 10. shipping_db: Shipments, Tracking Milestones, Carriers
-- -----------------------------------------------------------------------------
USE `shipping_db`;

CREATE TABLE IF NOT EXISTS `shipments` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_id` VARCHAR(36) NOT NULL UNIQUE,
    `carrier` VARCHAR(32) NOT NULL,
    `tracking_number` VARCHAR(64) NOT NULL UNIQUE,
    `shipping_method` VARCHAR(32) NOT NULL DEFAULT 'EXPRESS',
    `status` VARCHAR(32) NOT NULL DEFAULT 'IN_TRANSIT',
    `shipped_at` TIMESTAMP NULL,
    `estimated_delivery` TIMESTAMP NULL,
    `delivered_at` TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tracking_milestones` (
    `id` VARCHAR(36) PRIMARY KEY,
    `shipment_id` VARCHAR(36) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `recorded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `shipments` (`id`, `order_id`, `carrier`, `tracking_number`, `shipping_method`, `status`, `shipped_at`, `estimated_delivery`, `delivered_at`) VALUES
('ship-01', 'ord-01', 'DHL Express', 'DHL-9847291', 'EXPRESS', 'DELIVERED', '2026-11-15 09:30:00', '2026-11-16 16:00:00', '2026-11-16 14:22:00'),
('ship-02', 'ord-02', 'FedEx', 'FX-4820194', 'PRIORITY', 'IN_TRANSIT', '2026-11-16 11:00:00', '2026-11-18 18:00:00', NULL)
ON DUPLICATE KEY UPDATE status=VALUES(status);

INSERT INTO `tracking_milestones` (`id`, `shipment_id`, `status`, `location`, `description`) VALUES
('tm-01', 'ship-01', 'PICKED_UP', 'Newark, NJ Hub', 'Shipment collected from warehouse'),
('tm-02', 'ship-01', 'IN_TRANSIT', 'Portland, OR Sorting Center', 'Processed through sorting facility'),
('tm-03', 'ship-01', 'OUT_FOR_DELIVERY', 'Springfield, OR', 'Out with local courier for final delivery'),
('tm-04', 'ship-01', 'DELIVERED', 'Springfield, OR', 'Delivered to recipient with signature verified'),
('tm-05', 'ship-02', 'PICKED_UP', 'Oakland, CA Hub', 'Package received at carrier facility'),
('tm-06', 'ship-02', 'IN_TRANSIT', 'Salt Lake City, UT', 'Departed transit hub')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- -----------------------------------------------------------------------------
-- 11. review_db: Ratings, Reviews, Helpful Votes, Moderation
-- -----------------------------------------------------------------------------
USE `review_db`;

CREATE TABLE IF NOT EXISTS `reviews` (
    `id` VARCHAR(36) PRIMARY KEY,
    `product_id` VARCHAR(36) NOT NULL,
    `customer_id` VARCHAR(36) NOT NULL,
    `author_name` VARCHAR(100) NOT NULL,
    `rating` INT NOT NULL,
    `headline` VARCHAR(255) NOT NULL,
    `review_text` TEXT NOT NULL,
    `is_verified_purchase` BOOLEAN DEFAULT TRUE,
    `helpful_votes` INT DEFAULT 0,
    `status` VARCHAR(32) DEFAULT 'APPROVED',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `reviews` (`id`, `product_id`, `customer_id`, `author_name`, `rating`, `headline`, `review_text`, `is_verified_purchase`, `helpful_votes`) VALUES
('rev-01', 'prod-001', 'cust-01', 'John C.', 5, 'Incredible M3 Max performance for developers', 'Compiling massive microservice Java projects takes seconds. The Liquid Retina XDR screen is gorgeous and battery life lasts a full work day without charging.', TRUE, 34),
('rev-02', 'prod-001', 'cust-04', 'Alice S.', 5, 'Best workstation laptop money can buy', 'Zero fan noise even when running Docker containers, databases and IDEs simultaneously.', TRUE, 19),
('rev-03', 'prod-003', 'cust-01', 'John C.', 5, 'Unbeatable noise canceling for open offices', 'The Sony XM5 completely silences HVAC noise and background chatter. Soundstage is punchy and detailed.', TRUE, 27),
('rev-04', 'prod-004-iphone', 'cust-02', 'Sarah M.', 5, 'The camera control button is a game changer', 'Titanium finish feels remarkably light compared to older models. 4K 120fps video quality looks like a cinema camera.', TRUE, 42),
('rev-05', 'prod-013-ps5', 'cust-05', 'Bob J.', 5, 'Ultra fast load times and whisper quiet', 'Spider-Man 2 and Astro Bot play in pristine 4K 60fps with HDR. The DualSense haptic triggers add genuine immersion.', TRUE, 15)
ON DUPLICATE KEY UPDATE rating=VALUES(rating);

-- -----------------------------------------------------------------------------
-- 12. wishlist_db: Customer Saved Items & Wishlists
-- -----------------------------------------------------------------------------
USE `wishlist_db`;

CREATE TABLE IF NOT EXISTS `wishlists` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(100) NOT NULL DEFAULT 'My Saved Items',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `wishlist_items` (
    `id` VARCHAR(36) PRIMARY KEY,
    `wishlist_id` VARCHAR(36) NOT NULL,
    `sku` VARCHAR(64) NOT NULL,
    `product_name` VARCHAR(255) NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `image_url` VARCHAR(512),
    `added_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `wishlists` (`id`, `customer_id`, `name`) VALUES
('wl-01', 'cust-01', 'Dream Tech Setup'),
('wl-02', 'cust-04', 'Holiday Gifts')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO `wishlist_items` (`id`, `wishlist_id`, `sku`, `product_name`, `unit_price`, `image_url`) VALUES
('wli-01', 'wl-01', 'SKU-CAM-001', 'Sony Alpha a7 IV Full-Frame Camera', 2498.00, '/images/products/SKU-CAM-001.jpg'),
('wli-02', 'wl-01', 'SKU-HOM-002', 'Breville Barista Touch Espresso Machine', 999.95, '/images/products/SKU-HOM-002.jpg'),
('wli-03', 'wl-02', 'SKU-AUD-002', 'Bose QuietComfort Ultra Spatial Audio', 429.00, '/images/products/SKU-AUD-002.jpg')
ON DUPLICATE KEY UPDATE unit_price=VALUES(unit_price);

-- -----------------------------------------------------------------------------
-- 13. notification_db: Customer Alerts, Order Emails, SMS
-- -----------------------------------------------------------------------------
USE `notification_db`;

CREATE TABLE IF NOT EXISTS `notifications` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NOT NULL,
    `channel` VARCHAR(16) NOT NULL,
    `subject` VARCHAR(255) NOT NULL,
    `message_body` TEXT NOT NULL,
    `status` VARCHAR(32) DEFAULT 'SENT',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `read_at` TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `notifications` (`id`, `customer_id`, `channel`, `subject`, `message_body`, `status`, `read_at`) VALUES
('notif-01', 'cust-01', 'EMAIL', 'Order Confirmed - #ORD-2026-1001', 'Thank you for your order! Your MacBook Pro 16" and Sony XM5 headphones are being prepared for shipment.', 'SENT', '2026-11-15 10:00:00'),
('notif-02', 'cust-01', 'SMS', 'Your Package has been Delivered!', 'DHL Express tracking DHL-9847291 was delivered at 2:22 PM. Enjoy your new setup!', 'SENT', '2026-11-16 14:30:00'),
('notif-03', 'cust-02', 'EMAIL', 'Order Shipped - #ORD-2026-1002', 'Your iPhone 16 Pro Max is on the way via FedEx tracking FX-4820194.', 'SENT', NULL)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- -----------------------------------------------------------------------------
-- 14. returns_db: RMA Requests, Return Items, Audits
-- -----------------------------------------------------------------------------
USE `returns_db`;

CREATE TABLE IF NOT EXISTS `return_requests` (
    `id` VARCHAR(36) PRIMARY KEY,
    `order_id` VARCHAR(36) NOT NULL,
    `customer_id` VARCHAR(36) NOT NULL,
    `return_reason` VARCHAR(100) NOT NULL,
    `status` VARCHAR(32) DEFAULT 'PENDING_APPROVAL',
    `refund_amount` DECIMAL(10,2) NOT NULL,
    `requested_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `return_requests` (`id`, `order_id`, `customer_id`, `return_reason`, `status`, `refund_amount`) VALUES
('ret-01', 'ord-01', 'cust-01', 'Ordered wrong color variant', 'APPROVED', 398.00)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- -----------------------------------------------------------------------------
-- 15. recommendation_db: Behavioral Signals & Collaborative Recommendations
-- -----------------------------------------------------------------------------
USE `recommendation_db`;

CREATE TABLE IF NOT EXISTS `product_recommendations` (
    `id` VARCHAR(36) PRIMARY KEY,
    `source_sku` VARCHAR(64) NOT NULL,
    `recommended_sku` VARCHAR(64) NOT NULL,
    `score` DECIMAL(4,3) NOT NULL,
    `algorithm` VARCHAR(32) DEFAULT 'CO_PURCHASE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `product_recommendations` (`id`, `source_sku`, `recommended_sku`, `score`) VALUES
('rec-01', 'SKU-LAP-001', 'SKU-AUD-001', 0.950),
('rec-02', 'SKU-LAP-001', 'SKU-GAM-004', 0.880),
('rec-03', 'SKU-PHN-001', 'SKU-WCH-001', 0.940),
('rec-04', 'SKU-GAM-001', 'SKU-GAM-003', 0.890)
ON DUPLICATE KEY UPDATE score=VALUES(score);

-- -----------------------------------------------------------------------------
-- 16. ai_assistant_db: Chat Sessions, Messages, Knowledge Chunks
-- -----------------------------------------------------------------------------
USE `ai_assistant_db`;

CREATE TABLE IF NOT EXISTS `assistant_sessions` (
    `id` VARCHAR(36) PRIMARY KEY,
    `customer_id` VARCHAR(36) NOT NULL,
    `session_title` VARCHAR(128) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `assistant_messages` (
    `id` VARCHAR(36) PRIMARY KEY,
    `session_id` VARCHAR(36) NOT NULL,
    `role` VARCHAR(16) NOT NULL,
    `message_text` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `assistant_sessions` (`id`, `customer_id`, `session_title`) VALUES
('ai-sess-01', 'cust-01', 'Developer Laptop Comparison')
ON DUPLICATE KEY UPDATE session_title=VALUES(session_title);

INSERT INTO `assistant_messages` (`id`, `session_id`, `role`, `message_text`) VALUES
('msg-01', 'ai-sess-01', 'USER', 'Which laptop is better for Spring Boot microservices and Docker: MacBook Pro M3 or Dell XPS 15?'),
('msg-02', 'ai-sess-01', 'ASSISTANT', 'Both are outstanding workstations! The MacBook Pro 16" with M3 Max offers unmatched Unix native tooling, 32GB unified memory bandwidth, and silent operation. The Dell XPS 15 offers a breathtaking 3.5K OLED touch display and NVIDIA RTX 4070 graphics if you do gaming or CUDA AI workloads.')
ON DUPLICATE KEY UPDATE message_text=VALUES(message_text);

-- -----------------------------------------------------------------------------
-- 17. media_db: Media Assets & Processing Metadata
-- -----------------------------------------------------------------------------
USE `media_db`;

CREATE TABLE IF NOT EXISTS `media_assets` (
    `id` VARCHAR(36) PRIMARY KEY,
    `sku` VARCHAR(64) NOT NULL,
    `filename` VARCHAR(128) NOT NULL,
    `mime_type` VARCHAR(64) DEFAULT 'image/jpeg',
    `file_size_bytes` INT NOT NULL,
    `public_url` VARCHAR(512) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `media_assets` (`id`, `sku`, `filename`, `file_size_bytes`, `public_url`) VALUES
('med-01', 'SKU-LAP-001', 'SKU-LAP-001.jpg', 36675, '/images/products/SKU-LAP-001.jpg'),
('med-02', 'SKU-LAP-002', 'SKU-LAP-002.jpg', 47137, '/images/products/SKU-LAP-002.jpg'),
('med-03', 'SKU-LAP-003', 'SKU-LAP-003.jpg', 27001, '/images/products/SKU-LAP-003.jpg'),
('med-04', 'SKU-PHN-001', 'SKU-PHN-001.jpg', 22105, '/images/products/SKU-PHN-001.jpg'),
('med-05', 'SKU-PHN-002', 'SKU-PHN-002.jpg', 65363, '/images/products/SKU-PHN-002.jpg'),
('med-06', 'SKU-PHN-003', 'SKU-PHN-003.jpg', 97197, '/images/products/SKU-PHN-003.jpg'),
('med-07', 'SKU-AUD-001', 'SKU-AUD-001.jpg', 27288, '/images/products/SKU-AUD-001.jpg'),
('med-08', 'SKU-AUD-002', 'SKU-AUD-002.jpg', 21317, '/images/products/SKU-AUD-002.jpg'),
('med-09', 'SKU-AUD-003', 'SKU-AUD-003.jpg', 13973, '/images/products/SKU-AUD-003.jpg'),
('med-10', 'SKU-WCH-001', 'SKU-WCH-001.jpg', 16740, '/images/products/SKU-WCH-001.jpg'),
('med-11', 'SKU-WCH-002', 'SKU-WCH-002.jpg', 68903, '/images/products/SKU-WCH-002.jpg'),
('med-12', 'SKU-WCH-003', 'SKU-WCH-003.jpg', 33965, '/images/products/SKU-WCH-003.jpg'),
('med-13', 'SKU-GAM-001', 'SKU-GAM-001.jpg', 18597, '/images/products/SKU-GAM-001.jpg'),
('med-14', 'SKU-GAM-002', 'SKU-GAM-002.jpg', 16601, '/images/products/SKU-GAM-002.jpg'),
('med-15', 'SKU-GAM-003', 'SKU-GAM-003.jpg', 17749, '/images/products/SKU-GAM-003.jpg'),
('med-16', 'SKU-GAM-004', 'SKU-GAM-004.jpg', 45015, '/images/products/SKU-GAM-004.jpg'),
('med-17', 'SKU-CAM-001', 'SKU-CAM-001.jpg', 45164, '/images/products/SKU-CAM-001.jpg'),
('med-18', 'SKU-CAM-002', 'SKU-CAM-002.jpg', 17808, '/images/products/SKU-CAM-002.jpg'),
('med-19', 'SKU-HOM-001', 'SKU-HOM-001.jpg', 44923, '/images/products/SKU-HOM-001.jpg'),
('med-20', 'SKU-HOM-002', 'SKU-HOM-002.jpg', 50127, '/images/products/SKU-HOM-002.jpg')
ON DUPLICATE KEY UPDATE public_url=VALUES(public_url);
