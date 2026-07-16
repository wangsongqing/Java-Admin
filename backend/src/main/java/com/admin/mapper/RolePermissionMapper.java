package com.admin.mapper;

import com.admin.entity.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-权限关联 Mapper
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查询权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID列表查询权限编码列表（去重）
     */
    List<String> selectPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据用户ID查询权限编码列表（通过用户→角色→权限关联查询）
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据权限ID列表删除角色-权限关联
     */
    int deleteByPermissionIds(@Param("permissionIds") List<Long> permissionIds);
}
