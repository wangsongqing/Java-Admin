package com.admin.mapper;

import com.admin.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户-角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有角色关联
     */
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量查询多个用户的角色信息（返回 userId、roleId、roleName）
     */
    List<Map<String, Object>> selectRoleNamesByUserIds(@Param("userIds") List<Long> userIds);
}
