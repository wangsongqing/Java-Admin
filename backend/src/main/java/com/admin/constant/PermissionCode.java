package com.admin.constant;

/**
 * 权限码常量
 *
 * <p>集中管理 {@code @SaCheckPermission} 注解中使用的权限编码，
 * 避免在控制器中散落字符串字面量，减少拼写错误风险。</p>
 *
 * <p>权限编码需与 {@code permissions.yml} 及 {@code sys_permission} 表中的 code 保持一致。</p>
 */
public final class PermissionCode {

    private PermissionCode() {
    }

    // ---------- 用户管理 ----------
    public static final String USER_VIEW = "system:user:view";
    public static final String USER_ADD = "system:user:add";
    public static final String USER_EDIT = "system:user:edit";
    public static final String USER_DELETE = "system:user:delete";
    public static final String USER_RESET = "system:user:reset";
    public static final String USER_STATUS = "system:user:status";

    // ---------- 角色管理 ----------
    public static final String ROLE_VIEW = "system:role:view";
    public static final String ROLE_ADD = "system:role:add";
    public static final String ROLE_EDIT = "system:role:edit";
    public static final String ROLE_DELETE = "system:role:delete";
    public static final String ROLE_ASSIGN = "system:role:assign";
}
