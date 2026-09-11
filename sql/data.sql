-- ============================================
-- 大事件项目测试数据
-- 数据库: big_event
-- 版本: V1.0
-- 日期: 2026-09-11
-- ============================================

USE big_event;

-- 测试用户: 用户名 wangba, 密码 123456 (MD5: e10adc394ba59abbe56e057f20f883e5)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`)
VALUES ('wangba', 'e10adc394ba59abbe56e057f20f883e5', '王霸', 'wangba@example.com');

-- 测试分类
INSERT INTO `category` (`category_name`, `category_alias`, `create_user`)
VALUES
    ('美食', 'my', 1),
    ('风土人情', 'ftrq', 1),
    ('科技', 'tech', 1);

-- 测试文章
INSERT INTO `article` (`title`, `content`, `cover_img`, `state`, `category_id`, `create_user`)
VALUES
    ('北京烤鸭', '北京烤鸭是具有世界声誉的北京著名菜式...', 'https://example.com/duck.jpg', '已发布', 1, 1),
    ('胡同文化', '北京胡同是老北京文化的缩影...', 'https://example.com/hutong.jpg', '草稿', 2, 1);
