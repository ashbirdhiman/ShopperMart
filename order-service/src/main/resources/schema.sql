-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create order_items table for ElementCollection mapping
CREATE TABLE IF NOT EXISTS order_items (
    order_id BIGINT NOT NULL,
    sku_code VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (order_id, sku_code),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
