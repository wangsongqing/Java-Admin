-- ============================================
-- 为 sys_user 添加 email / phone 唯一索引
-- 配合「用户名 / 邮箱 / 手机号」登录与注册时的唯一性校验，提供 DB 级约束（防并发重复）
-- ============================================

-- 背景：此前曾通过一个未提交的本地 V5 脚本创建过 uk_email / uk_phone，
--      后因 V5 版本号冲突（两个 V5 文件）被清理，但索引已残留在库中。
--      本次将其正式纳入 Flyway 版本管理。
-- 注意：执行前需确认 sys_user 上不存在同名索引（即先手动删除残留索引），
--      否则 ADD UNIQUE KEY 会因 "Duplicate key name" 失败。
-- 说明：email / phone 列可为 NULL，MySQL 唯一索引允许多个 NULL，不影响未填写的用户。

UPDATE `sys_user` SET `phone` = NULL WHERE `phone` = '';
UPDATE `sys_user` SET `email` = NULL WHERE `email` = '';

ALTER TABLE `sys_user`
    ADD UNIQUE KEY `uk_email` (`email`);

ALTER TABLE `sys_user`
    ADD UNIQUE KEY `uk_phone` (`phone`);
