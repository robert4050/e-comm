-- ==============================================================================
-- V4: Add Brand Column to Products Table
-- ==============================================================================

ALTER TABLE products MODIFY COLUMN brand VARCHAR(100) NULL AFTER category;

-- Seed brand values for existing products based on name
UPDATE products SET brand = 'Apple' WHERE name LIKE '%Apple%' OR name LIKE '%MacBook%' OR name LIKE '%AirPods%' OR name LIKE '%iPhone%' OR name LIKE '%Watch Ultra%';
UPDATE products SET brand = 'Samsung' WHERE name LIKE '%Samsung%' OR name LIKE '%Galaxy%';
UPDATE products SET brand = 'Sony' WHERE name LIKE '%Sony%' OR name LIKE '%PlayStation%';
UPDATE products SET brand = 'Dell' WHERE name LIKE '%Dell%';
UPDATE products SET brand = 'Lenovo' WHERE name LIKE '%Lenovo%' OR name LIKE '%ThinkPad%';
UPDATE products SET brand = 'Google' WHERE name LIKE '%Google%' OR name LIKE '%Pixel%';
UPDATE products SET brand = 'Bose' WHERE name LIKE '%Bose%';
UPDATE products SET brand = 'Garmin' WHERE name LIKE '%Garmin%';
UPDATE products SET brand = 'Keychron' WHERE name LIKE '%Keychron%';
UPDATE products SET brand = 'Logitech' WHERE name LIKE '%Logitech%';
UPDATE products SET brand = 'Dyson' WHERE name LIKE '%Dyson%';
UPDATE products SET brand = 'Breville' WHERE name LIKE '%Breville%';
UPDATE products SET brand = 'Microsoft' WHERE name LIKE '%Xbox%';
UPDATE products SET brand = 'Fujifilm' WHERE name LIKE '%Fuji%';
