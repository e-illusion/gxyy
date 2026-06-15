-- GXYY 校园以物易物平台 数据库初始化脚本
-- 请先创建数据库: CREATE DATABASE gxyy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(20) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 初始分类数据
INSERT INTO `category` (`id`, `name`, `icon`, `sort_order`) VALUES
(1, '数码电子', '📱', 1),
(2, '书籍教材', '📚', 2),
(3, '生活用品', '🏠', 3),
(4, '服饰鞋包', '👗', 4),
(5, '运动器材', '⚽', 5),
(6, '美妆护肤', '💄', 6),
(7, '玩具乐器', '🎮', 7),
(8, '其他', '📦', 99);

-- 物品表
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `owner_id` BIGINT NOT NULL COMMENT '发布者ID',
    `category_id` INT NOT NULL COMMENT '分类ID',
    `title` VARCHAR(100) NOT NULL COMMENT '物品名称',
    `condition` VARCHAR(20) NOT NULL COMMENT '成色: NEW/LIKE_NEW/SLIGHTLY_USED/NORMAL_USE',
    `description` TEXT NOT NULL COMMENT '物品描述',
    `want_description` VARCHAR(500) DEFAULT NULL COMMENT '想换什么',
    `images` VARCHAR(2000) DEFAULT '[]' COMMENT '图片JSON数组',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/EXCHANGED/OFF_SHELF',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品表';

-- 交换请求表
CREATE TABLE IF NOT EXISTS `exchange_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `from_user_id` BIGINT NOT NULL COMMENT '发起方用户ID',
    `to_user_id` BIGINT NOT NULL COMMENT '接收方(物主)用户ID',
    `from_item_id` BIGINT NOT NULL COMMENT '发起方拿出的物品ID',
    `to_item_id` BIGINT NOT NULL COMMENT '目标物品ID',
    `message` VARCHAR(500) DEFAULT NULL COMMENT '留言',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/ACCEPTED/REJECTED/CANCELLED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_from_user` (`from_user_id`),
    KEY `idx_to_user` (`to_user_id`),
    KEY `idx_to_item` (`to_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交换请求表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
    `type` VARCHAR(30) NOT NULL COMMENT '通知类型',
    `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID(如exchange_request.id)',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `exchange_id` BIGINT NOT NULL COMMENT '关联的交换请求ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_exchange_id` (`exchange_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
