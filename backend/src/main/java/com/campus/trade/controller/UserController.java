package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.PasswordDTO;
import com.campus.trade.dto.UpdateUserInfoDTO;
import com.campus.trade.entity.SysUser;
import com.campus.trade.entity.UserAddress;
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<SysUser> getUserInfo() {
        return Result.success(userService.getUserInfo(SecurityUtil.getCurrentUserId()));
    }

    @ApiOperation("修改个人信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@Valid @RequestBody UpdateUserInfoDTO dto) {
        userService.updateUserInfo(SecurityUtil.getCurrentUserId(), dto);
        return Result.success();
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO dto) {
        userService.updatePassword(SecurityUtil.getCurrentUserId(), dto);
        return Result.success();
    }

    @ApiOperation("修改头像")
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody String avatarUrl) {
        userService.updateAvatar(SecurityUtil.getCurrentUserId(), avatarUrl);
        return Result.success();
    }

    @ApiOperation("获取收货地址列表")
    @GetMapping("/address")
    public Result<List<UserAddress>> getAddressList() {
        return Result.success(userService.getAddressList(SecurityUtil.getCurrentUserId()));
    }

    @ApiOperation("保存收货地址")
    @PostMapping("/address")
    public Result<Void> saveAddress(@RequestBody UserAddress address) {
        userService.saveAddress(SecurityUtil.getCurrentUserId(), address);
        return Result.success();
    }

    @ApiOperation("设置默认收货地址")
    @PutMapping("/address/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        userService.setDefaultAddress(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("删除收货地址")
    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }
}
