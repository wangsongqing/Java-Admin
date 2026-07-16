package com.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体（菜单 + 按钮 + API 权限统一存储）
 */
@Data
@TableName("sys_permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 子权限（非数据库字段） */
    @TableField(exist = false)
    private List<Permission> children;

    /** 权限ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父级ID，顶级为0 */
    private Long parentId;

    /** 权限名称 */
    private String name;

    /** 权限编码，如 system:user:add */
    private String code;

    /** 类型：menu-菜单 button-按钮 api-接口 */
    private String type;

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

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
