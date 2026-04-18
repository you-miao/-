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
import org.springframework.util.StringUtils;

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
        String nickname = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        if (!StringUtils.hasText(user.getNickname())) {
            SysUser repair = new SysUser();
            repair.setId(user.getId());
            repair.setNickname(user.getUsername());
            userMapper.updateById(repair);
        }
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", nickname);
        result.put("avatar", user.getAvatar());
        result.put("role", loginUser.getRole());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        String nickname = StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : username;
        String realName = dto.getRealName().trim();
        String phone = StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null;
        String studentNo = dto.getStudentNo().trim();

        if ("admin".equalsIgnoreCase(username) || "charity".equalsIgnoreCase(username)) {
            throw new BusinessException("该用户名为系统保留账号，请更换");
        }
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (phone != null) {
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }
        Long stuCount = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getStudentNo, studentNo));
        if (stuCount > 0) {
            throw new BusinessException("学号已被注册");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(nickname);
        user.setRealName(realName);
        user.setGender(dto.getGender());
        user.setPhone(phone);
        user.setStudentNo(studentNo);
        user.setStatus(1);
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L);
        userRoleMapper.insert(userRole);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        String value = username.trim();
        if ("admin".equalsIgnoreCase(value) || "charity".equalsIgnoreCase(value)) {
            return false;
        }
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, value));
        return count == 0;
    }

    @Override
    public boolean isStudentNoAvailable(String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return false;
        }
        String value = studentNo.trim();
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getStudentNo, value));
        return count == 0;
    }
}
