package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.LoginDTO;
import com.campus.trade.dto.RegisterDTO;
import com.campus.trade.entity.SysLoginLog;
import com.campus.trade.entity.SysUser;
import com.campus.trade.entity.SysUserRole;
import com.campus.trade.mapper.SysLoginLogMapper;
import com.campus.trade.mapper.SysUserMapper;
import com.campus.trade.mapper.SysUserRoleMapper;
import com.campus.trade.security.JwtUtil;
import com.campus.trade.security.LoginUser;
import com.campus.trade.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysLoginLogMapper loginLogMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(LoginDTO dto, String ip, String device) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(loginUser.getUserId(), loginUser.getUsername(), loginUser.getRole());

        SysUser user = userMapper.selectById(loginUser.getUserId());
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        SysLoginLog log = new SysLoginLog();
        log.setUserId(loginUser.getUserId());
        log.setUsername(loginUser.getUsername());
        log.setLoginType(1);
        log.setLoginIp(ip);
        log.setDevice(device);
        log.setStatus(1);
        log.setMsg("登录成功");
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", loginUser.getRole());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (dto.getPhone() != null) {
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, dto.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }
        if (dto.getStudentNo() != null) {
            Long stuCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getStudentNo, dto.getStudentNo()));
            if (stuCount > 0) {
                throw new BusinessException("学号已被注册");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setGender(dto.getGender());
        user.setPhone(dto.getPhone());
        user.setStudentNo(dto.getStudentNo());
        user.setCampus(dto.getCampus());
        user.setStatus(1);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L);
        userRoleMapper.insert(userRole);
    }
}
