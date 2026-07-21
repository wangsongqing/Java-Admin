-- ============================================
-- 新增查看权限：system:user:view / system:role:view
-- 1) 插入权限（幂等，按 code 去重）
-- 2) 分配给 ROLE_ADMIN（全部）和 ROLE_USER（仅查看）
-- ============================================

-- 插入 system:user:view（查看用户）
INSERT IGNORE INTO `sys_permission` (`parent_id`, `name`, `code`, `type`, `sort`)
    SELECT id, '查看用户', 'system:user:view', 'button', 0
    FROM `sys_permission` WHERE `code` = 'system:user';

-- 插入 system:role:view（查看角色）
INSERT IGNORE INTO `sys_permission` (`parent_id`, `name`, `code`, `type`, `sort`)
    SELECT id, '查看角色', 'system:role:view', 'button', 0
    FROM `sys_permission` WHERE `code` = 'system:role';

-- 分配给 ROLE_ADMIN（超级管理员）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
    SELECT r.id, p.id
    FROM `sys_role` r, `sys_permission` p
    WHERE r.role_code = 'ROLE_ADMIN' AND p.code = 'system:user:view';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
    SELECT r.id, p.id
    FROM `sys_role` r, `sys_permission` p
    WHERE r.role_code = 'ROLE_ADMIN' AND p.code = 'system:role:view';

-- 分配给 ROLE_USER（普通用户：仅查看）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
    SELECT r.id, p.id
    FROM `sys_role` r, `sys_permission` p
    WHERE r.role_code = 'ROLE_USER' AND p.code = 'system:user:view';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
    SELECT r.id, p.id
    FROM `sys_role` r, `sys_permission` p
    WHERE r.role_code = 'ROLE_USER' AND p.code = 'system:role:view';
