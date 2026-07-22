package com.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.admin.common.Result;
import com.admin.constant.PermissionCode;
import com.admin.constant.RoleCode;
import com.admin.dto.*;
import com.admin.entity.User;
import com.admin.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 需要 ROLE_ADMIN 角色才能访问
 */
@RestController
@RequestMapping("/users")
@SaCheckRole(RoleCode.ADMIN)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    @SaCheckPermission(PermissionCode.USER_VIEW)
    public Result<Page<User>> page(UserQueryDTO queryDTO) {
        return Result.success(userService.getUserPage(queryDTO));
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @SaCheckPermission(PermissionCode.USER_VIEW)
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getCurrentUser(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    @SaCheckPermission(PermissionCode.USER_ADD)
    public Result<User> create(@Valid @RequestBody CreateUserDTO createDTO) {
        return Result.success("创建成功", userService.createUser(createDTO));
    }

    /**
     * 更新用户
     */
    @PutMapping
    @SaCheckPermission(PermissionCode.USER_EDIT)
    public Result<User> update(@Valid @RequestBody UpdateUserDTO updateDTO) {
        return Result.success("更新成功", userService.updateUser(updateDTO));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission(PermissionCode.USER_DELETE)
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    @SaCheckPermission(PermissionCode.USER_RESET)
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success("密码重置成功", null);
    }

    /**
     * 切换用户状态
     */
    @PutMapping("/{id}/status")
    @SaCheckPermission(PermissionCode.USER_STATUS)
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.toggleStatus(id, status);
        return Result.success("状态更新成功", null);
    }
}
