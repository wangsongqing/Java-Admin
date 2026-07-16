package com.admin.service;

import com.admin.dto.*;
import com.admin.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser(Long userId);

    /**
     * 分页查询用户列表
     */
    Page<User> getUserPage(UserQueryDTO queryDTO);

    /**
     * 创建用户
     */
    User createUser(CreateUserDTO createDTO);

    /**
     * 更新用户
     */
    User updateUser(UpdateUserDTO updateDTO);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 重置密码
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 切换用户状态
     */
    boolean toggleStatus(Long id, Integer status);
}
