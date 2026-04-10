package com.campus.trade.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.entity.SysUser;
import com.campus.trade.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .or()
                        .eq(SysUser::getPhone, username)
                        .or()
                        .eq(SysUser::getStudentNo, username));
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        String role = sysUserMapper.selectRoleKeyByUserId(user.getId());
        if (role == null) {
            role = "ROLE_USER";
        }
        return new LoginUser(user, role);
    }
}
