package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.Result;
import com.campus.trade.dto.AuditDTO;
import com.campus.trade.dto.ProductQueryDTO;
import com.campus.trade.entity.Comment;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.SysUser;
import com.campus.trade.entity.DonationCampaign; // 💡 新增导入
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.AdminService;
import com.campus.trade.service.CommentService;
import com.campus.trade.service.ProductService;
import com.campus.trade.service.UserService;
import com.campus.trade.service.DonationService; // 💡 新增导入
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "后台管理接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;
    @Resource
    private ProductService productService;
    @Resource
    private UserService userService;
    @Resource
    private CommentService commentService;
    @Resource
    private DonationService donationService; // 💡 新增注入

    @ApiOperation("仪表盘统计")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(adminService.getDashboardStats());
    }

    @ApiOperation("审核操作")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody AuditDTO dto) {
        adminService.audit(SecurityUtil.getCurrentUserId(), dto);
        return Result.success();
    }

    @ApiOperation("商品管理列表")
    @GetMapping("/products")
    public Result<IPage<Product>> productList(ProductQueryDTO query) {
        return Result.success(productService.getProductPage(query));
    }

    @ApiOperation("用户管理列表")
    @GetMapping("/users")
    public Result<IPage<SysUser>> userList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(userService.getUserPage(pageNum, pageSize, keyword));
    }

    @ApiOperation("启用/禁用用户")
    @PutMapping("/user/status/{userId}")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success();
    }

    @ApiOperation("评论管理列表")
    @GetMapping("/comments")
    public Result<IPage<Comment>> commentList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(commentService.getAdminCommentPage(status, pageNum, pageSize));
    }

    // ================= 💡 新增：捐赠活动审核相关接口 =================

    @ApiOperation("捐赠活动管理列表")
    @GetMapping("/donation/list")
    public Result<IPage<DonationCampaign>> donationList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        // 返回包含待审核状态的列表
        return Result.success(donationService.getAdminDonationPage(status, pageNum, pageSize));
    }

    // 修改后
    @ApiOperation("审核捐赠活动")
    @PostMapping("/donation/audit")
    public Result<String> auditDonation(@RequestBody AuditDTO auditDTO) {
        return donationService.auditDonationCampaign(auditDTO);
    }
}