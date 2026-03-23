/*
 Navicat Premium Data Transfer

 Source Server         : 本地localhosts数据库
 Source Server Type    : MySQL
 Source Server Version : 80024
 Source Host           : localhost:3306
 Source Schema         : aipaint

 Target Server Type    : MySQL
 Target Server Version : 80024
 File Encoding         : 65001

 Date: 23/03/2026 15:30:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_generate_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_generate_log`;
CREATE TABLE `ai_generate_log`  (
                                    `id` bigint(0) NOT NULL AUTO_INCREMENT,
                                    `drawing_id` bigint(0) NULL DEFAULT NULL,
                                    `prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                                    `result_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                                    `status` tinyint(0) NULL DEFAULT 0 COMMENT '状态：0-处理中，1-成功，2-失败',
                                    `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ai生成日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for drawing
-- ----------------------------
DROP TABLE IF EXISTS `drawing`;
CREATE TABLE `drawing`  (
                            `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '绘图ID',
                            `user_id` bigint(0) NOT NULL COMMENT '用户ID',
                            `drawing_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户上传的绘图URL',
                            `ai_image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI生成的图像URL',
                            `style` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '绘画风格',
                            `status` tinyint(0) NULL DEFAULT 0 COMMENT '状态：0-处理中，1-成功，2-失败',
                            `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '绘图记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                         `openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信 openid',
                         `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
                         `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
                         `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
                         `gender` tinyint(0) NULL DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
                         `birthday` date NULL DEFAULT NULL COMMENT '生日',
                         `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
                         `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
                         `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家',
                         `login_count` int(0) NULL DEFAULT 0 COMMENT '登录次数',
                         `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
                         `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
                         `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                         `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uk_openid`(`openid`, `phone_number`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
