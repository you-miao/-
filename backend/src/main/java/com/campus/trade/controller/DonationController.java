package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.DonationAnnouncement;
import com.campus.trade.entity.DonationOrder;
import com.campus.trade.entity.DonationCampaign;
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

    @GetMapping("/public/campaigns")
    public Result getCampaigns() {
        return Result.success(donationService.getActiveCampaigns());
    }

    @GetMapping("/public/announcements")
    public Result getAnnouncements() {
        return Result.success(donationService.getAnnouncements());
    }

    // 💡 修复点：将 hasAnyRole 统一改为 hasAnyAuthority，完美兼容数据库中的 admin 和 user 标识
    @PostMapping("/user/submit")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'ROLE_USER', 'ROLE_ADMIN')")
    public Result submitDonation(@RequestBody DonationOrder order) {
        Long userId = SecurityUtil.getLoginUser().getUserId();
        donationService.submitDonationOrder(order, userId);
        return Result.success("捐赠提交成功，感谢您的爱心！");
    }

    @GetMapping("/user/my-list")
    @PreAuthorize("hasAnyAuthority('user', 'admin', 'ROLE_USER', 'ROLE_ADMIN')")
    public Result getMyDonations() {
        Long userId = SecurityUtil.getLoginUser().getUserId();
        List<DonationOrder> list = donationService.getMyDonations(userId);
        return Result.success(list);
    }

    @GetMapping("/charity/orders")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result getAllOrders(@RequestParam(required = false) Long campaignId,
                               @RequestParam(required = false) Integer status) {
        List<DonationOrder> list;
        if (campaignId != null) {
            list = donationService.getCampaignOrders(campaignId, status);
        } else {
            list = donationService.getAllOrders();
        }
        return Result.success(list);
    }

    @GetMapping("/charity/campaigns")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result getCharityCampaigns() {
        Long charityId = SecurityUtil.getLoginUser().getUserId();
        return Result.success(donationService.getCharityCampaigns(charityId));
    }

    @PostMapping("/charity/receive/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result receiveDonation(@PathVariable Long orderId) {
        Long charityId = SecurityUtil.getLoginUser().getUserId();
        boolean success = donationService.receiveDonation(orderId, charityId);
        if (success) {
            return Result.success("已确认接收物资");
        }
        return Result.error("接收失败，订单可能不存在或已被处理");
    }

    @PostMapping("/charity/reject/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result rejectDonation(@PathVariable Long orderId,
                                 @RequestParam(required = false) String reason) {
        Long charityId = SecurityUtil.getLoginUser().getUserId();
        boolean success = donationService.rejectDonation(orderId, charityId, reason);
        if (success) {
            return Result.success("已驳回该捐赠订单");
        }
        return Result.error("驳回失败，订单可能不存在或状态已变更");
    }

    @PostMapping("/charity/campaign/publish")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result publishCampaign(@RequestBody DonationCampaign campaign) {
        // 绑定当前发布者的 ID
        campaign.setCharityId(SecurityUtil.getLoginUser().getUserId());

        boolean success = donationService.publishCampaign(campaign);
        if (success) {
            return Result.success("募捐活动发布成功，请等待管理员审核！");
        }
        return Result.error("发布失败，请检查数据");
    }

    @PostMapping("/charity/announcement/publish")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result publishAnnouncement(@RequestBody DonationAnnouncement announcement) {
        Long charityId = SecurityUtil.getLoginUser().getUserId();
        boolean success = donationService.publishAnnouncement(announcement, charityId);
        if (success) {
            return Result.success("公告发布成功");
        }
        return Result.error("公告发布失败");
    }

    @PutMapping("/charity/campaign/offline/{campaignId}")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result offlineCampaign(@PathVariable Long campaignId) {
        Long charityId = SecurityUtil.getLoginUser().getUserId();
        boolean success = donationService.offlineCampaign(campaignId, charityId);
        if (success) {
            return Result.success("活动已下架");
        }
        return Result.error("下架失败，仅可操作自己发布的活动");
    }

    @PutMapping("/charity/campaign/target/{campaignId}")
    @PreAuthorize("hasAuthority('ROLE_CHARITY')")
    public Result adjustTarget(@PathVariable Long campaignId,
                               @RequestParam Integer delta) {
        Long charityId = SecurityUtil.getLoginUser().getUserId();
        boolean success = donationService.adjustCampaignTarget(campaignId, charityId, delta);
        if (success) {
            return Result.success("目标数量已更新");
        }
        return Result.error("更新失败，请检查活动归属或调整值");
    }
}
