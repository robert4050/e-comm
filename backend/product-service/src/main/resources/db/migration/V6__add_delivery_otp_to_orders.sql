-- ==============================================================================
-- V6: Add Delivery OTP and Customer Phone to Orders for Twilio Verification
-- ==============================================================================

ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(32) DEFAULT '+91 98765 43210',
ADD COLUMN IF NOT EXISTS delivery_otp VARCHAR(16) NULL,
ADD COLUMN IF NOT EXISTS otp_verified BOOLEAN DEFAULT FALSE;

-- Seed initial OTP for existing demonstration orders
UPDATE orders SET delivery_otp = '582910', customer_phone = '+91 98765 43210' WHERE id = 'ord-103';
UPDATE orders SET delivery_otp = '749102', customer_phone = '+91 98765 43210' WHERE id = 'ord-102';
UPDATE orders SET delivery_otp = '610394', customer_phone = '+91 98765 43210' WHERE id = 'ord-101';
UPDATE orders SET otp_verified = TRUE WHERE fulfilment_status = 'DELIVERED';
