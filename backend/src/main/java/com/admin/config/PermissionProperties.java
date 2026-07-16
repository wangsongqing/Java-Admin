package com.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限配置属性（绑定 permissions.yml）
 *
 * <p>通过 {@link ConfigurationProperties} 将 {@code permissions.definitions} 下的 YAML 列表
 * 绑定到 {@link #definitions} 字段。应用启动时由 {@link PermissionSyncRunner} 读取并同步到数据库。</p>
 */
@Data
@ConfigurationProperties(prefix = "permissions")
public class PermissionProperties {

    /**
     * 是否同步删除：true = YAML 中不存在的权限会从数据库级联删除（默认 false，安全模式）
     */
    private boolean syncDelete = false;

    /**
     * 权限定义列表
     */
    private List<PermissionDefinition> definitions = new ArrayList<>();

    /**
     * 单条权限定义
     */
    @Data
    public static class PermissionDefinition {

        /** 权限编码，全局唯一，如 system:user:add */
        private String code;

        /** 权限名称 */
        private String name;

        /** 类型：menu-菜单 button-按钮 api-接口 */
        private String type;

        /** 父级权限的 code，顶级菜单为空或 null */
        private String parentCode;

        /** 菜单图标 */
        private String icon;

        /** 路由地址 */
        private String path;

        /** 组件路径 */
        private String component;

        /** 排序 */
        private Integer sort;

        /** 状态：1-正常 0-禁用 */
        private Integer status;
    }
}
