package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.role_key FROM sys_user_role ur JOIN sys_role r ON ur.role_id = r.id WHERE ur.user_id = #{userId} LIMIT 1")
    String selectRoleKeyByUserId(@Param("userId") Long userId);

    @Update("UPDATE sys_user SET balance = balance + #{amount} WHERE id = #{userId} AND deleted = 0")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Update("UPDATE sys_user SET balance = balance - #{amount} WHERE id = #{userId} AND balance >= #{amount} AND deleted = 0")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
