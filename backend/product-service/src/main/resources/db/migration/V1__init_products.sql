-- ==============================================================================
-- V1: Initial Schema for Product Microservice
-- ==============================================================================

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    stock_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload LONGTEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initial Seed Data
INSERT INTO products (id, sku, name, description, price, category, status, stock_quantity, image_url, version)
VALUES 
('prod-001', 'SKU-LAP-001', 'Pro Laptop 16" M3', 'Flagship workstation laptop featuring 32GB RAM, 1TB SSD, and Liquid Retina display.', 1999.99, 'Electronics', 'PUBLISHED', 45, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600', 1),
('prod-002', 'SKU-PHN-002', 'Flagship Smartphone Ultra', 'Next-gen smartphone with AMOLED 120Hz display, 200MP camera system and 5000mAh battery.', 1099.00, 'Electronics', 'PUBLISHED', 80, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600', 1),
('prod-003', 'SKU-AUD-003', 'Wireless Noise-Canceling Headphones', 'Premium over-ear headphones with active noise cancellation and 40h battery playback.', 349.50, 'Audio', 'PUBLISHED', 120, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', 1),
('prod-004', 'SKU-WCH-004', 'Smart Fitness Watch Series 9', 'Rugged outdoor smartwatch with heart-rate monitoring, GPS tracking and waterproof design.', 299.00, 'Wearables', 'PUBLISHED', 60, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600', 1),
('prod-005', 'SKU-DSK-005', 'Ergonomic Mechanical Keyboard', 'Custom mechanical keyboard with hot-swappable switches, RGB backlight and aluminum frame.', 149.99, 'Accessories', 'PUBLISHED', 200, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600', 1)
ON DUPLICATE KEY UPDATE name=name;
