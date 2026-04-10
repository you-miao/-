package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.dto.PasswordDTO;
import com.campus.trade.entity.SysUser;
import com.campus.trade.entity.UserAddress;

import java.util.List;

public interface UserService {
    SysUser getUserInfo(Long userId);
    void updateUserInfo(SysUser user);
    void updatePassword(Long userId, PasswordDTO dto);
    void updateAvatar(Long userId, String avatarUrl);
    List<UserAddress> getAddressList(Long userId);
    void saveAddress(Long userId, UserAddress address);
    void deleteAddress(Long userId, Long addressId);
    IPage<SysUser> getUserPage(int pageNum, int pageSize, String keyword);
    void updateUserStatus(Long userId, Integer status);
}
