-- 初始化管理员用户 (密码: Admin123456)
-- 使用 INSERT IGNORE 避免重复插入错误
INSERT IGNORE INTO users (username, password, email, nickname, created_at, updated_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '管理员', NOW(), NOW());
