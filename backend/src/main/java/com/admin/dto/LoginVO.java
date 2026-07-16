package com.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录返回VO
 */
@Data
@Builder
public class LoginVO {

    /** Token */
    private String token;

    /** 过期时间（秒） */
    private Long expiresIn;

    /** 用户信息 */
    private UserInfo userInfo;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        /** 角色编码列表 */
        private List<String> roles;
        /** 权限编码列表 */
        private List<String> permissions;
    }
}
