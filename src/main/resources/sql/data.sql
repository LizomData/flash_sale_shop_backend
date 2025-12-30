INSERT INTO user_account (id, name, phone, password, role, balance)
VALUES (1, 'admin', '18800000000', '123456', 'admin', 2000.00)
ON DUPLICATE KEY UPDATE name = VALUES(name), password = VALUES(password), role = VALUES(role), balance = VALUES(balance);

INSERT INTO product (id, name, description, tag, price, stock)
VALUES
    (1, 'AirPods Pro 2', '低延迟音频，搭配秒杀优惠价', '3C', 1299.00, 30),
    (2, '保温杯 · 冬日限定', '316 不锈钢，长效保温', '居家', 159.00, 120),
    (3, '机械键盘 98 配列', '热插拔轴体，RGB 背光', '外设', 499.00, 60),
    (4, '石墨烯暖贴', '升温快，冬日保暖', '健康', 39.00, 200)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), tag = VALUES(tag), price = VALUES(price), stock = VALUES(stock);

INSERT INTO seckill_event (id, title, product_id, seckill_price, limit_per_user, stock, initial_stock, sold, start_at, end_at)
VALUES
    ('se-1', '耳机闪购 50% OFF', 1, 899.00, 1, 15, 15, 0, UNIX_TIMESTAMP(NOW()) * 1000 - 600000, UNIX_TIMESTAMP(NOW()) * 1000 + 2400000),
    ('se-2', '保温杯整点秒杀', 2, 89.00, 2, 80, 80, 0, UNIX_TIMESTAMP(NOW()) * 1000 + 600000, UNIX_TIMESTAMP(NOW()) * 1000 + 4200000),
    ('se-3', '暖贴买二送一', 4, 19.00, 3, 0, 120, 120, UNIX_TIMESTAMP(NOW()) * 1000 - 1800000, UNIX_TIMESTAMP(NOW()) * 1000 - 300000)
ON DUPLICATE KEY UPDATE title = VALUES(title), seckill_price = VALUES(seckill_price), limit_per_user = VALUES(limit_per_user),
    stock = VALUES(stock), initial_stock = VALUES(initial_stock), sold = VALUES(sold), start_at = VALUES(start_at), end_at = VALUES(end_at);
