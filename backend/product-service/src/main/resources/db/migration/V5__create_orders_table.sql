-- ==============================================================================
-- V5: Create Orders Table for Razorpay Checkout & Fulfilment
-- ==============================================================================

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    order_number VARCHAR(32) NOT NULL UNIQUE,
    customer_name VARCHAR(128) NOT NULL,
    customer_email VARCHAR(128) NOT NULL,
    items_count INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(32) NOT NULL DEFAULT 'PAID',
    payment_method VARCHAR(32) NOT NULL DEFAULT 'RAZORPAY',
    razorpay_payment_id VARCHAR(64) NULL,
    fulfilment_status VARCHAR(32) NOT NULL DEFAULT 'PROCESSING',
    carrier VARCHAR(64) NOT NULL DEFAULT 'FedEx Express',
    tracking_number VARCHAR(64) NOT NULL DEFAULT 'TRK-98231',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initial Seed Orders
INSERT INTO orders (id, order_number, customer_name, customer_email, items_count, total_price, payment_status, payment_method, razorpay_payment_id, fulfilment_status, carrier, tracking_number, created_at)
VALUES 
('ord-101', 'ORD-84920', 'Alex Rivera', 'alex.rivera@example.com', 2, 2499.99, 'PAID', 'RAZORPAY', 'pay_Nzd8f90248a', 'PROCESSING', 'FedEx Priority', 'FDX-9948201', NOW()),
('ord-102', 'ORD-84921', 'Marcus Vance', 'marcus.v@corporate.io', 1, 1099.00, 'PAID', 'RAZORPAY', 'pay_Nzd8f90248b', 'CONFIRMED', 'DHL Express', 'DHL-3382109', NOW()),
('ord-103', 'ORD-84922', 'Sophia Chen', 'sophia.c@design.co', 3, 1899.00, 'PAID', 'RAZORPAY', 'pay_Nzd8f90248c', 'SHIPPED', 'UPS Worldwide', '1Z9999999999999999', NOW()),
('ord-104', 'ORD-84923', 'David Miller', 'david.m@techlab.org', 1, 249.00, 'PAID', 'RAZORPAY', 'pay_Nzd8f90248d', 'DELIVERED', 'FedEx Home', 'FDX-7738291', NOW()),
('ord-105', 'ORD-84924', 'Emma Watson', 'emma.w@domain.com', 1, 149.99, 'REFUNDED', 'RAZORPAY', 'pay_Nzd8f90248e', 'CANCELLED', 'Standard', 'N/A', NOW())
ON DUPLICATE KEY UPDATE order_number=order_number;
