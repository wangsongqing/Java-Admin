package com.admin.service.impl;

import com.admin.entity.Permission;
import com.admin.entity.RolePermission;
import com.admin.mapper.RolePermissionMapper;
import com.admin.service.PermissionService;
import com.admin.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<com.admin.mapper.RoleMapper, com.admin.entity.Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionService permissionService;

    @Override
    public List<com.admin.entity.Role> listByKeyword(String keyword) {
        LambdaQueryWrapper<com.admin.entity.Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(com.admin.entity.Role::getRoleName, keyword)
                    .or().like(com.admin.entity.Role::getRoleCode, keyword));
        }
        wrapper.orderByAsc(com.admin.entity.Role::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除该角色所有旧权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));

        // 批量插入新权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }
        log.info("角色[{}]分配权限完成，共{}个权限", roleId, permissionIds == null ? 0 : permissionIds.size());
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
}
