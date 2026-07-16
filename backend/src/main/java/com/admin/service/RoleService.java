package com.admin.service;

import com.admin.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     */
    List<Role> listByKeyword(String keyword);

    /**
     * 分配权限（覆盖式）
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 查询角色拥有的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
}
