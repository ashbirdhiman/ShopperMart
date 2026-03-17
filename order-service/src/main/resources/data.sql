-- ====================================================================
-- Order Service Test Data
-- ====================================================================
-- This file contains INSERT statements for testing the order service
-- Database: order_service
-- ====================================================================

-- Insert test orders (customer_id matches user_id from user_service)
INSERT INTO orders (customer_id, total_price, status, created_at) VALUES
(1, 999.99, 'CONFIRMED', '2024-01-15 11:20:00'),
(2, 2199.98, 'CONFIRMED', '2024-01-15 11:25:00'),
(3, 1299.99, 'PENDING', '2024-01-15 11:30:00'),
(4, 799.99, 'CONFIRMED', '2024-01-15 11:35:00'),
(5, 299.98, 'CONFIRMED', '2024-01-15 11:40:00'),
(1, 299.97, 'PENDING', '2024-01-15 11:45:00'),
(2, 399.99, 'CONFIRMED', '2024-01-15 11:50:00'),
(3, 599.99, 'CONFIRMED', '2024-01-15 11:55:00'),
(4, 899.99, 'PENDING', '2024-01-15 12:00:00'),
(5, 1499.99, 'CONFIRMED', '2024-01-15 12:05:00'),
(6, 99.95, 'CONFIRMED', '2024-01-15 12:10:00'),
(7, 159.98, 'PENDING', '2024-01-15 12:15:00'),
(8, 799.99, 'CONFIRMED', '2024-01-15 12:20:00'),
(9, 1199.98, 'CONFIRMED', '2024-01-15 12:25:00'),
(10, 898.00, 'PENDING', '2024-01-15 12:30:00'),
(1, 499.98, 'CONFIRMED', '2024-01-15 12:35:00'),
(2, 1099.99, 'CONFIRMED', '2024-01-15 12:40:00'),
(3, 999.99, 'PENDING', '2024-01-15 12:45:00'),
(4, 149.99, 'CONFIRMED', '2024-01-15 12:50:00'),
(5, 199.98, 'CONFIRMED', '2024-01-15 12:55:00');

-- Insert order items (sku_code -> quantity mapping)
INSERT INTO order_items (order_id, sku_code, quantity) VALUES
(1, 'SKU-LAPTOP-001', 1),
(2, 'SKU-PHONE-001', 2),
(3, 'SKU-TABLET-001', 1),
(4, 'SKU-MONITOR-001', 1),
(5, 'SKU-KEYBOARD-001', 2),
(6, 'SKU-MOUSE-001', 3),
(7, 'SKU-HEADPHONE-001', 1),
(8, 'SKU-LAPTOP-002', 1),
(9, 'SKU-PHONE-002', 1),
(10, 'SKU-CAMERA-001', 1),
(11, 'SKU-MOUSE-002', 5),
(12, 'SKU-KEYBOARD-002', 2),
(13, 'SKU-TABLET-002', 1),
(14, 'SKU-MONITOR-002', 2),
(15, 'SKU-CAMERA-002', 1),
(16, 'SKU-HEADPHONE-002', 2),
(17, 'SKU-KEYBOARD-003', 1),
(18, 'SKU-MONITOR-003', 1),
(19, 'SKU-PHONE-003', 1),
(20, 'SKU-CAMERA-003', 2);
