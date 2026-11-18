-- ==============================================================================
-- V3: Create Users Table & Seed Super Admin User
-- ==============================================================================

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL DEFAULT 'USER',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Initial Seed Data:
-- 1. Super Admin: admin@gmail.com / admin1234 (SUPER_ADMIN)
-- 2. Administrator: manager@e-comm.local / admin1234 (ADMIN)
-- 3. User: customer@e-comm.local / admin1234 (USER)
-- BCrypt cost 10 hash for "admin1234": $2a$10$dBCcLj9mSGSRmsx6rGwz3uD3ze0OQlvxa/fMhxW8uwN0MMsyhkada
INSERT INTO users (id, email, password_hash, full_name, role, status, avatar_url)
VALUES 
('usr_superadmin_01', 'admin@gmail.com', '$2a$10$dBCcLj9mSGSRmsx6rGwz3uD3ze0OQlvxa/fMhxW8uwN0MMsyhkada', 'Super Administrator', 'SUPER_ADMIN', 'ACTIVE', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80'),
('usr_manager_01', 'manager@e-comm.local', '$2a$10$7q5XU2K2lD9yNl5Y9eWw6.b8/yG1r.YQWp2K9Gk0T8yP.yF1k2k', 'Sarah Administrator', 'ADMIN', 'ACTIVE', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop&q=80'),
('usr_customer_01', 'customer@e-comm.local', '$2a$10$7q5XU2K2lD9yNl5Y9eWw6.b8/yG1r.YQWp2K9Gk0T8yP.yF1k2k', 'Alex Customer', 'USER', 'ACTIVE', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), role=VALUES(role), status=VALUES(status);
