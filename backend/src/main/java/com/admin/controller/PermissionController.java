package com.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.admin.common.Result;
import com.admin.entity.Permission;
import com.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/permissions")
@SaCheckRole("ROLE_ADMIN")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 查询所有权限树
     */
    @GetMapping("/tree")
    public Result<List<Permission>> tree() {
        return Result.success(permissionService.tree());
    }
}
