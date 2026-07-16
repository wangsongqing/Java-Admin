package com.admin.service.impl;

import com.admin.entity.Permission;
import com.admin.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<com.admin.mapper.PermissionMapper, Permission> implements PermissionService {

    @Override
    public List<Permission> tree() {
        // 查询所有未删除的权限
        List<Permission> allPermissions = list(new LambdaQueryWrapper<Permission>()
                .orderByAsc(Permission::getSort));

        // 按 parentId 分组
        Map<Long, List<Permission>> childrenMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null && p.getParentId() != 0)
                .collect(Collectors.groupingBy(Permission::getParentId));

        // 为每个权限设置 children
        for (Permission permission : allPermissions) {
            List<Permission> children = childrenMap.get(permission.getId());
            permission.setChildren(children != null ? children : new ArrayList<>());
        }

        // 返回顶级权限（parentId == 0）
        return allPermissions.stream()
                .filter(p -> p.getParentId() != null && p.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
