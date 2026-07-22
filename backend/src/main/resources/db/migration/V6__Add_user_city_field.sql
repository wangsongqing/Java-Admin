-- ============================================
--  V6: sys_user 新增 city 字段（城市）
-- ============================================

ALTER TABLE `sys_user`
    ADD COLUMN `city` VARCHAR(50) DEFAULT NULL COMMENT '城市' AFTER `remark`;
