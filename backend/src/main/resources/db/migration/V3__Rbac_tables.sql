-- ============================================
-- RBAC 权限体系：角色表、权限表、关联表
-- ============================================

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码，如 ROLE_ADMIN',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-正常 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限表（菜单 + 按钮 + API 权限统一存储）
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID，顶级为0',
    `name`        VARCHAR(50)  NOT NULL COMMENT '权限名称',
    `code`        VARCHAR(100) NOT NULL COMMENT '权限编码，如 system:user:add',
    `type`        VARCHAR(20)  NOT NULL COMMENT '类型：menu-菜单 button-按钮 api-接口',
    `icon`        VARCHAR(50)  DEFAULT NULL COMMENT '菜单图标',
    `path`        VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    `component`   VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-正常 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `role_id`       BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `remark`) VALUES
    ('ROLE_ADMIN', '超级管理员', '拥有所有权限'),
    ('ROLE_USER',  '普通用户',   '基础查看权限');

-- 初始化权限（菜单 + 按钮）
-- 系统管理（顶级菜单）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `icon`, `path`, `component`, `sort`) VALUES
    (1, 0, '系统管理', 'system', 'menu', 'Setting', NULL, NULL, 1);

-- 用户管理
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `icon`, `path`, `component`, `sort`) VALUES
    (2, 1, '用户管理', 'system:user', 'menu', 'User', '/users', 'views/user/index', 1),
    (3, 2, '新增用户', 'system:user:add', 'button', NULL, NULL, NULL, 1),
    (4, 2, '编辑用户', 'system:user:edit', 'button', NULL, NULL, NULL, 2),
    (5, 2, '删除用户', 'system:user:delete', 'button', NULL, NULL, NULL, 3),
    (6, 2, '重置密码', 'system:user:reset', 'button', NULL, NULL, NULL, 4),
    (7, 2, '切换状态', 'system:user:status', 'button', NULL, NULL, NULL, 5);

-- 角色管理
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `icon`, `path`, `component`, `sort`) VALUES
    (8, 1, '角色管理', 'system:role', 'menu', 'UserFilled', '/roles', 'views/role/index', 2),
    (9, 8, '新增角色', 'system:role:add', 'button', NULL, NULL, NULL, 1),
    (10, 8, '编辑角色', 'system:role:edit', 'button', NULL, NULL, NULL, 2),
    (11, 8, '删除角色', 'system:role:delete', 'button', NULL, NULL, NULL, 3),
    (12, 8, '分配权限', 'system:role:assign', 'button', NULL, NULL, NULL, 4);

-- 将全部权限赋给超级管理员
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12);

-- 普通用户只有查看权限（系统管理菜单 + 用户管理菜单，无按钮权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
    (2, 1), (2, 2);

-- 为现有用户分配角色（admin → ROLE_ADMIN，test → ROLE_USER）
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2);
