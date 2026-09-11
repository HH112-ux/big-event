-- ============================================
-- 大事件项目数据库 DDL
-- 数据库: big_event
-- 版本: V1.0
-- 日期: 2026-09-11
-- ============================================

CREATE DATABASE IF NOT EXISTS big_event DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE big_event;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `username`    VARCHAR(16)  NOT NULL COMMENT '用户名, 5~16位',
    `password`    VARCHAR(64)  NOT NULL COMMENT '密码 (MD5加密存储)',
    `nickname`    VARCHAR(10)  DEFAULT NULL COMMENT '昵称, 1~10位',
    `email`       VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
    `user_pic`    VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 文章分类表
-- ============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类主键',
    `category_name`  VARCHAR(32) NOT NULL COMMENT '分类名称',
    `category_alias` VARCHAR(32) NOT NULL COMMENT '分类别名',
    `create_user`    BIGINT      NOT NULL COMMENT '创建者用户ID',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_name` (`create_user`, `category_name`),
    KEY `idx_user_alias` (`create_user`, `category_alias`),
    CONSTRAINT `fk_category_user` FOREIGN KEY (`create_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- ============================================
-- 3. 文章表
-- ============================================
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文章主键',
    `title`       VARCHAR(10)  NOT NULL COMMENT '文章标题, 1~10字符',
    `content`     TEXT         NOT NULL COMMENT '文章正文',
    `cover_img`   VARCHAR(512) NOT NULL COMMENT '封面图URL',
    `state`       VARCHAR(8)   NOT NULL COMMENT '发布状态: 已发布/草稿',
    `category_id` BIGINT       NOT NULL COMMENT '所属分类ID',
    `create_user` BIGINT       NOT NULL COMMENT '作者用户ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`     INT          NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-正常 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_state` (`category_id`, `state`, `create_user`),
    KEY `idx_create_user` (`create_user`),
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
    CONSTRAINT `fk_article_user` FOREIGN KEY (`create_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
