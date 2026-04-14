package com.campus.trade.service;

import com.campus.trade.entity.DonationCampaign;
import com.campus.trade.entity.DonationOrder;
import com.campus.trade.entity.DonationAnnouncement;

import java.util.List;

public interface DonationService {

    // ================= C端：普通用户接口 =================

    // 获取正在进行中的募捐活动
    List<DonationCampaign> getActiveCampaigns();

    // 获取公示栏信息
    List<DonationAnnouncement> getAnnouncements();

    // 获取当前用户的捐赠记录
    List<DonationOrder> getMyDonations(Long userId);

    // 提交捐赠单（包含一键转捐赠逻辑）
    void submitDonationOrder(DonationOrder order, Long currentUserId);


    // ================= B端：社团管理端接口 =================

    // 1. 获取所有用户的捐赠订单（供后台表格查看）
    List<DonationOrder> getAllOrders();

    // 2. 社团确认接收物资 (💡注意：这里已经帮你改成 boolean 了！)
    boolean receiveDonation(Long orderId, Long charityId);

    // 3. 发布新的募捐活动
    boolean publishCampaign(DonationCampaign campaign);
}