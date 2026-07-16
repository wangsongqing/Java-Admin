package com.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新用户DTO
 */
@Data
public class UpdateUserDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;

    private String email;

    private String phone;

    private Integer gender;

    private Integer status;

    /** 角色ID列表 */
    private List<Long> roleIds;

    private String remark;
}
