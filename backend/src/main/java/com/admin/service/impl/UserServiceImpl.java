package com.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.admin.dto.CreateUserDTO;
import com.admin.dto.LoginDTO;
import com.admin.dto.UpdateUserDTO;
import com.admin.dto.UserQueryDTO;
import com.admin.entity.User;
import com.admin.entity.UserRole;
import com.admin.vo.LoginVO;
import com.admin.enums.StatusEnum;
import com.admin.exception.BusinessException;
import com.admin.mapper.RolePermissionMapper;
import com.admin.mapper.UserMapper;
import com.admin.mapper.UserRoleMapper;
import com.admin.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 查询用户（支持用户名/邮箱/手机号登录）
        User user = lambdaQuery()
                .and(w -> w.eq(User::getUsername, loginDTO.getAccount())
                        .or().eq(User::getEmail, loginDTO.getAccount())
                        .or().eq(User::getPhone, loginDTO.getAccount()))
                .one();

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 校验密码（BCrypt）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验状态
        if (StatusEnum.isDisabled(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 4. Sa-Token 登录
        StpUtil.login(user.getId());

        // 5. 查询用户角色和权限
        java.util.List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        java.util.List<String> permissions = rolePermissionMapper.selectPermissionCodesByUserId(user.getId());

        // 6. 构造返回
        return LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .expiresIn(86400L)
                .userInfo(LoginVO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .roles(roles)
                        .permissions(permissions)
                        .build())
                .build();
    }

    @Override
    public User getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public Page<User> getUserPage(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 关键字查询
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, queryDTO.getKeyword())
                    .or().like(User::getNickname, queryDTO.getKeyword())
                    .or().like(User::getEmail, queryDTO.getKeyword()));
        }
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        // 角色筛选（通过角色ID关联查询）
        if (queryDTO.getRoleId() != null) {
            wrapper.inSql(User::getId,
                    "SELECT user_id FROM sys_user_role WHERE role_id = " + queryDTO.getRoleId());
        } else if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        page = page(page, wrapper);

        // 填充角色信息
        fillRoleInfo(page.getRecords());

        return page;
    }

    /**
     * 批量填充用户的角色信息
     */
    private void fillRoleInfo(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Map<String, Object>> roleMaps = userRoleMapper.selectRoleNamesByUserIds(userIds);

        // 按 userId 分组
        Map<Long, List<Long>> userIdToRoleIds = new HashMap<>();
        Map<Long, List<String>> userIdToRoleNames = new HashMap<>();
        for (Map<String, Object> map : roleMaps) {
            Long userId = ((Number) map.get("userId")).longValue();
            Long roleId = ((Number) map.get("roleId")).longValue();
            String roleName = (String) map.get("roleName");
            userIdToRoleIds.computeIfAbsent(userId, k -> new ArrayList<>()).add(roleId);
            userIdToRoleNames.computeIfAbsent(userId, k -> new ArrayList<>()).add(roleName);
        }

        for (User user : users) {
            user.setRoleIds(userIdToRoleIds.getOrDefault(user.getId(), Collections.emptyList()));
            user.setRoleNames(userIdToRoleNames.getOrDefault(user.getId(), Collections.emptyList()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(CreateUserDTO createDTO) {
        // 1. 校验用户名唯一性
        long count = lambdaQuery().eq(User::getUsername, createDTO.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 校验邮箱唯一性
        if (StringUtils.hasText(createDTO.getEmail())) {
            long emailCount = lambdaQuery().eq(User::getEmail, createDTO.getEmail()).count();
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }

        // 3. 校验手机号唯一性
        if (StringUtils.hasText(createDTO.getPhone())) {
            long phoneCount = lambdaQuery().eq(User::getPhone, createDTO.getPhone()).count();
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被使用");
            }
        }

        // 2. 构造用户
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        // 3. 保存用户
        save(user);

        // 4. 保存角色关联
        if (createDTO.getRoleIds() != null && !createDTO.getRoleIds().isEmpty()) {
            for (Long roleId : createDTO.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        log.info("创建用户成功: {}", user.getUsername());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(UpdateUserDTO updateDTO) {
        User user = getById(updateDTO.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段
        if (StringUtils.hasText(updateDTO.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getEmail() != null) user.setEmail(updateDTO.getEmail());
        if (updateDTO.getPhone() != null) user.setPhone(updateDTO.getPhone());
        if (updateDTO.getGender() != null) user.setGender(updateDTO.getGender());
        if (updateDTO.getStatus() != null) user.setStatus(updateDTO.getStatus());
        if (updateDTO.getRemark() != null) user.setRemark(updateDTO.getRemark());

        updateById(user);

        // 更新角色关联（覆盖式）
        userRoleMapper.deleteByUserId(user.getId());
        if (updateDTO.getRoleIds() != null && !updateDTO.getRoleIds().isEmpty()) {
            for (Long roleId : updateDTO.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        log.info("更新用户成功: {}", user.getUsername());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 不允许删除指定手机号的保护用户
        if ("18200000001".equals(user.getPhone())) {
            throw new BusinessException("该用户不允许删除");
        }
        // 不允许删除自己
        // 不允许删除超级管理员（可扩展）

        // 清理角色关联
        userRoleMapper.deleteByUserId(id);

        boolean result = removeById(id);
        if (result) {
            log.info("删除用户成功: {}", user.getUsername());
        }
        return result;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public boolean toggleStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        return updateById(user);
    }
}
