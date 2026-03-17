-- 创建数据库
CREATE DATABASE IF NOT EXISTS aipaint CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE aipaint;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    avatar VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 作品表
CREATE TABLE IF NOT EXISTS drawing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    drawing_url VARCHAR(500) NOT NULL,
    ai_image_url VARCHAR(500),
    style VARCHAR(50),
    status INT DEFAULT 0 COMMENT '0: 待生成, 1: 已生成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- AI生成记录表
CREATE TABLE IF NOT EXISTS ai_generate_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drawing_id BIGINT NOT NULL,
    prompt TEXT NOT NULL,
    result_url VARCHAR(500),
    status INT DEFAULT 0 COMMENT '0: 处理中, 1: 成功, 2: 失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (drawing_id) REFERENCES drawing(id)
);