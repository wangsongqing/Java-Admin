package com.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.admin.common.Result;
import com.admin.dto.LoginDTO;
import com.admin.entity.User;
import com.admin.vo.LoginVO;
import com.admin.mapper.RolePermissionMapper;
import com.admin.mapper.UserRoleMapper;
import com.admin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器（登录/用户信息/登出）
 * 注意：登录接口通过 sa-token.exclude-path 放行，无需 @NoAuth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 登录（无需登录即可访问）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", userService.login(loginDTO));
    }

    /**
     * 获取当前用户信息（包含角色、权限）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = Long.valueOf(StpUtil.getLoginId().toString());
        User user = userService.getCurrentUser(userId);
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = rolePermissionMapper.selectPermissionCodesByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("roles", roles);
        data.put("permissions", permissions);
        return Result.success(data);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("登出成功", null);
    }
}
