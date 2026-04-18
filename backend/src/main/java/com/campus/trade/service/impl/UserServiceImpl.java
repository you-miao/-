package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.PasswordDTO;
import com.campus.trade.dto.UpdateUserInfoDTO;
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
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

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
            if (!StringUtils.hasText(user.getNickname()) && StringUtils.hasText(user.getUsername())) {
                SysUser repair = new SysUser();
                repair.setId(userId);
                repair.setNickname(user.getUsername());
                userMapper.updateById(repair);
                user.setNickname(user.getUsername());
            }
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserInfoDTO dto) {
        if (StringUtils.hasText(dto.getPhone())) {
            Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, dto.getPhone())
                    .ne(SysUser::getId, userId));
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被其他账号使用");
            }
        }
        if (StringUtils.hasText(dto.getEmail())) {
            Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, dto.getEmail())
                    .ne(SysUser::getId, userId));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他账号使用");
            }
        }
        Long studentNoCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStudentNo, dto.getStudentNo())
                .ne(SysUser::getId, userId));
        if (studentNoCount > 0) {
            throw new BusinessException("学号已被其他账号使用");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(dto.getNickname().trim());
        update.setRealName(dto.getRealName().trim());
        update.setGender(dto.getGender());
        update.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        update.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        update.setStudentNo(dto.getStudentNo().trim());
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
        normalizeDefaultAddress(userId, null);
        return queryUserAddresses(userId);
    }

    @Override
    public void saveAddress(Long userId, UserAddress address) {
        String receiver = StringUtils.hasText(address.getReceiver()) ? address.getReceiver().trim() : null;
        String phone = StringUtils.hasText(address.getPhone()) ? address.getPhone().trim() : null;
        String campus = StringUtils.hasText(address.getCampus()) ? address.getCampus().trim() : null;
        String detail = StringUtils.hasText(address.getDetail()) ? address.getDetail().trim() : null;

        if (!StringUtils.hasText(receiver) || !StringUtils.hasText(phone)
                || !StringUtils.hasText(campus) || !StringUtils.hasText(detail)) {
            throw new BusinessException("收货人、电话、校区和详细地址不能为空");
        }
        if (!MOBILE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException("联系电话格式不正确");
        }

        if (address.getId() != null) {
            Long existCount = addressMapper.selectCount(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getId, address.getId())
                    .eq(UserAddress::getUserId, userId));
            if (existCount == 0) {
                throw new BusinessException("地址不存在或无权限修改");
            }
        }

        Long duplicateCount = addressMapper.selectCount(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getReceiver, receiver)
                .eq(UserAddress::getPhone, phone)
                .eq(UserAddress::getCampus, campus)
                .eq(UserAddress::getDetail, detail)
                .ne(address.getId() != null, UserAddress::getId, address.getId()));
        if (duplicateCount > 0) {
            throw new BusinessException("地址内容重复，请勿重复添加");
        }

        address.setUserId(userId);
        address.setReceiver(receiver);
        address.setPhone(phone);
        address.setCampus(campus);
        address.setDetail(detail);
        address.setIsDefault(address.getIsDefault() != null && address.getIsDefault() == 1 ? 1 : 0);

        if (address.getIsDefault() == 1) {
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
        normalizeDefaultAddress(userId, address.getId());
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, addressId)
                .eq(UserAddress::getUserId, userId));
        if (count == 0) {
            throw new BusinessException("地址不存在或无权限操作");
        }

        addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
        UserAddress update = new UserAddress();
        update.setId(addressId);
        update.setIsDefault(1);
        addressMapper.updateById(update);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        int deleted = addressMapper.delete(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getId, addressId)
                        .eq(UserAddress::getUserId, userId));
        if (deleted == 0) {
            throw new BusinessException("地址不存在或无权限删除");
        }
        normalizeDefaultAddress(userId, null);
    }

    private List<UserAddress> queryUserAddresses(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreateTime));
    }

    private void normalizeDefaultAddress(Long userId, Long preferredAddressId) {
        List<UserAddress> list = queryUserAddresses(userId);
        if (list.isEmpty()) {
            return;
        }

        UserAddress target = null;
        if (preferredAddressId != null) {
            for (UserAddress item : list) {
                if (preferredAddressId.equals(item.getId())) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null) {
            for (UserAddress item : list) {
                if (item.getIsDefault() != null && item.getIsDefault() == 1) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null) {
            target = list.get(0);
        }

        Long targetId = target.getId();
        addressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .ne(UserAddress::getId, targetId)
                .set(UserAddress::getIsDefault, 0));
        if (target.getIsDefault() == null || target.getIsDefault() != 1) {
            UserAddress update = new UserAddress();
            update.setId(targetId);
            update.setIsDefault(1);
            addressMapper.updateById(update);
        }
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
                SysUser::getBalance, SysUser::getStatus, SysUser::getCreateTime,
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
