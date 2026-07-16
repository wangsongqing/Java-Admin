package com.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import com.admin.mapper.RolePermissionMapper;
import com.admin.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源实现
 * 告诉 Sa-Token：一个登录账号有哪些角色、哪些权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 返回一个账号所拥有的权限码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return rolePermissionMapper.selectPermissionCodesByUserId(userId);
    }

    /**
     * 返回一个账号所拥有的角色标识列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return userRoleMapper.selectRoleCodesByUserId(userId);
    }
}
