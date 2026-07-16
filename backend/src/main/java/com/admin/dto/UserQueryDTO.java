package com.admin.dto;

import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO {

    /** 当前页 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;

    /** 关键字（用户名/昵称/邮箱） */
    private String keyword;

    /** 状态 */
    private Integer status;

    /** 角色（兼容旧字段） */
    private String role;

    /** 角色ID（按角色筛选） */
    private Long roleId;
}
