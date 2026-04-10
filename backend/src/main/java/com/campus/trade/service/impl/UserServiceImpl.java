package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.PasswordDTO;
import com.campus.trade.entity.SysUser;
import com.campus.trade.entity.UserAddress;
import com.campus.trade.mapper.SysUserMapper;
import com.campus.trade.mapper.UserAddressMapper;
import com.campus.trade.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private UserAddressMapper addressMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public void updateUserInfo(SysUser user) {
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setNickname(user.getNickname());
        update.setRealName(user.getRealName());
        update.setGender(user.getGender());
        update.setPhone(user.getPhone());
        update.setEmail(user.getEmail());
        update.setCampus(user.getCampus());
        userMapper.updateById(update);
    }

    @Override
    public void updatePassword(Long userId, PasswordDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(update);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setAvatar(avatarUrl);
        userMapper.updateById(update);
    }

    @Override
    public List<UserAddress> getAddressList(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreateTime));
    }

    @Override
    public void saveAddress(Long userId, UserAddress address) {
        address.setUserId(userId);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.update(null,
                    new LambdaUpdateWrapper<UserAddress>()
                            .eq(UserAddress::getUserId, userId)
                            .set(UserAddress::getIsDefault, 0));
        }
        if (address.getId() != null) {
            addressMapper.updateById(address);
        } else {
            addressMapper.insert(address);
        }
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        addressMapper.delete(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getId, addressId)
                        .eq(UserAddress::getUserId, userId));
    }

    @Override
    public IPage<SysUser> getUserPage(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword)
                    .or().like(SysUser::getStudentNo, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getNickname,
                SysUser::getRealName, SysUser::getPhone, SysUser::getStudentNo,
                SysUser::getCampus, SysUser::getBalance, SysUser::getStatus, SysUser::getCreateTime,
                SysUser::getLastLoginTime, SysUser::getAvatar, SysUser::getGender);
        return userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
    }
}
