package com.admin.constant;

/**
 * 角色编码常量
 *
 * <p>集中管理 {@code @SaCheckRole} 注解中使用的角色编码，
 * 需与 {@code sys_role} 表中的 role_code 保持一致。</p>
 */
public final class RoleCode {

    private RoleCode() {
    }

    /** 超级管理员，拥有所有权限 */
    public static final String ADMIN = "ROLE_ADMIN";

    /** 普通用户 */
    public static final String USER = "ROLE_USER";
}
