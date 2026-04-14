// File: controller/DonationController.java
package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.DonationOrder;
import com.campus.trade.entity.DonationCampaign; // 💡 新增引入活动实体类
import com.campus.trade.service.DonationService;
import com.campus.trade.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    // ================= 公开接口 (大厅展示) =================

    @GetMapping("/public/campaigns")
    public Result getCampaigns() {
        return Result.success(donationService.getActiveCampaigns());
    }

    @GetMapping("/public/announcements")
    public Result getAnnouncements() {
        return Result.success(donationService.getAnnouncements());
    }

    // ================= C端：用户接口（普通用户捐赠） =================

    @PostMapping("/user/submit")
    @PreAuthorize("hasAnyRole('USER', 'CHARITY', 'ADMIN')")
    public Result submitDonation(@RequestBody DonationOrder order) {
        Long userId = SecurityUtil.getLoginUser().getUserId();
        donationService.submitDonationOrder(order, userId);
        return Result.success("捐赠提交成功，感谢您的爱心！");
    }

    @GetMapping("/user/my-list")
    @PreAuthorize("hasAnyRole('USER', 'CHARITY', 'ADMIN')")
    public Result getMyDonations() {
        Long userId = SecurityUtil.getLoginUser().getUserId();
        List<DonationOrder> list = donationService.getMyDonations(userId);
        return Result.success(list);
    }

    // ================= B端：社团专属工作台接口 =================

    /**
     * 1. 获取所有用户的捐赠订单（供社团后台查看）
     */
    @GetMapping("/charity/orders")
    @PreAuthorize("hasAnyRole('CHARITY', 'ADMIN')")
    public Result getAllOrders() {
        List<DonationOrder> list = donationService.getAllOrders();
        return Result.success(list);
    }

    /**
     * 2. 确认接收物资
     */
    @PostMapping("/charity/receive/{orderId}")
    @PreAuthorize("hasAnyRole('CHARITY', 'ADMIN')")
    public Result receiveDonation(@PathVariable Long orderId) {
        Long charityId = SecurityUtil.getLoginUser().getUserId();

        // 💡 修复点：利用 boolean 返回值判断是否成功
        boolean success = donationService.receiveDonation(orderId, charityId);
        if (success) {
            return Result.success("已确认接收物资");
        }
        return Result.error("接收失败，订单可能不存在或已被处理");
    }

    /**
     * 3. 发布新募捐活动
     */
    @PostMapping("/charity/campaign/publish")
    @PreAuthorize("hasAnyRole('CHARITY', 'ADMIN')")
    public Result publishCampaign(@RequestBody DonationCampaign campaign) {
        boolean success = donationService.publishCampaign(campaign);
        if (success) {
            return Result.success("募捐活动发布成功！大厅已同步更新。");
        }
        return Result.error("发布失败，请检查数据");
    }
}