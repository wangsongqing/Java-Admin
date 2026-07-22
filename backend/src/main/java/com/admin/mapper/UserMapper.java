package com.admin.mapper;

import com.admin.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 通过账号（用户名/邮箱/手机号）查询用户
     * <p>
     * 使用 UNION ALL 拆分三条独立查询，确保每条分别命中 uk_username / uk_email / uk_phone 唯一索引，
     * 避免 OR 跨列查询导致的 index merge 或全表扫描。
     * </p>
     *
     * @param account 用户输入的账号
     * @return 匹配的用户，未找到返回 null
     */
    User selectByAccount(@Param("account") String account);
}
