CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    tag VARCHAR(32),
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS seckill_event (
    id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    product_id BIGINT NOT NULL,
    seckill_price DECIMAL(12, 2) NOT NULL,
    limit_per_user INT NOT NULL DEFAULT 1,
    stock INT NOT NULL DEFAULT 0,
    initial_stock INT NOT NULL DEFAULT 0,
    sold INT NOT NULL DEFAULT 0,
    start_at BIGINT NOT NULL,
    end_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    item_key VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    item_type VARCHAR(16) NOT NULL,
    event_id VARCHAR(32),
    price DECIMAL(12, 2) NOT NULL,
    qty INT NOT NULL,
    UNIQUE KEY uk_cart_user_item (user_id, item_key)
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(32) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at VARCHAR(32) NOT NULL,
    total DECIMAL(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(32) NOT NULL,
    item_key VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    item_type VARCHAR(16) NOT NULL,
    event_id VARCHAR(32),
    price DECIMAL(12, 2) NOT NULL,
    qty INT NOT NULL
);
