package com.admin.service;

import com.admin.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 查询权限树（所有权限按父子结构组织）
     */
    List<Permission> tree();
}
