package com.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.admin.dto.*;
import com.admin.entity.User;
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
        // 1. 查询用户
        User user = lambdaQuery()
                .eq(User::getUsername, loginDTO.getUsername())
                .one();

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 校验密码（BCrypt）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验状态
        if (user.getStatus() == 0) {
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
        // 角色筛选
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(User::getRole, queryDTO.getRole());
        }
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(CreateUserDTO createDTO) {
        // 1. 校验用户名唯一性
        long count = lambdaQuery().eq(User::getUsername, createDTO.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 构造用户
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        // 3. 保存
        save(user);
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
        if (StringUtils.hasText(updateDTO.getRole())) user.setRole(updateDTO.getRole());
        if (updateDTO.getRemark() != null) user.setRemark(updateDTO.getRemark());

        updateById(user);
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
