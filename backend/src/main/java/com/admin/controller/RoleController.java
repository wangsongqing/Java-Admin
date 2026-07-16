package com.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.admin.common.Result;
import com.admin.entity.Role;
import com.admin.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@SaCheckRole("ROLE_ADMIN")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    public Result<List<Role>> list(@RequestParam(required = false) String keyword) {
        return Result.success(roleService.listByKeyword(keyword));
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 新增角色
     */
    @PostMapping
    @SaCheckPermission("system:role:add")
    public Result<Role> create(@Valid @RequestBody Role role) {
        roleService.save(role);
        return Result.success("创建成功", role);
    }

    /**
     * 更新角色
     */
    @PutMapping
    @SaCheckPermission("system:role:edit")
    public Result<Role> update(@Valid @RequestBody Role role) {
        roleService.updateById(role);
        return Result.success("更新成功", roleService.getById(role.getId()));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 查询角色拥有的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }

    /**
     * 分配权限（覆盖式）
     */
    @PutMapping("/{id}/permissions")
    @SaCheckPermission("system:role:assign")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success("分配成功", null);
    }
}
