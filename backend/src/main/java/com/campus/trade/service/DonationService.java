package com.campus.trade.service;

import com.campus.trade.common.Result;
import com.campus.trade.dto.AuditDTO;
import com.campus.trade.entity.DonationCampaign;
import com.campus.trade.entity.DonationOrder;
import com.campus.trade.entity.DonationAnnouncement;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 * 爱心捐赠服务接口
 */
public interface DonationService {

    // ================= C端：普通用户接口 =================

    /**
     * 获取正在进行中的募捐活动（状态为已通过的）
     */
    List<DonationCampaign> getActiveCampaigns();

    /**
     * 获取公示栏公告信息
     */
    List<DonationAnnouncement> getAnnouncements();

    /**
     * 获取指定用户的个人捐赠记录
     */
    List<DonationOrder> getMyDonations(Long userId);

    /**
     * 提交捐赠申请
     * @param order 捐赠订单信息
     * @param currentUserId 当前操作用户ID
     */
    void submitDonationOrder(DonationOrder order, Long currentUserId);


    // ================= B端：社团管理端接口 =================

    /**
     * 获取所有捐赠订单记录（用于社团后台管理）
     */
    List<DonationOrder> getAllOrders();

    /**
     * 获取当前社团发布的活动（包含审核状态）
     */
    List<DonationCampaign> getCharityCampaigns(Long charityId);

    /**
     * 按活动查询订单（可选状态）
     */
    List<DonationOrder> getCampaignOrders(Long campaignId, Integer status);

    /**
     * 社团确认接收捐赠物资
     * @param orderId 订单ID
     * @param charityId 执行接收的社团/机构ID
     * @return 是否处理成功
     */
    boolean receiveDonation(Long orderId, Long charityId);

    /**
     * 社团驳回捐赠订单
     */
    boolean rejectDonation(Long orderId, Long charityId, String reason);

    /**
     * 发布新的募捐活动（初始状态通常为待审核）
     */
    boolean publishCampaign(DonationCampaign campaign);

    /**
     * 下架活动（社团主动结束）
     */
    boolean offlineCampaign(Long campaignId, Long charityId);

    /**
     * 调整目标数量（正数增加，负数减少）
     */
    boolean adjustCampaignTarget(Long campaignId, Long charityId, Integer delta);

    /**
     * 发布公示公告（图文）
     */
    boolean publishAnnouncement(DonationAnnouncement announcement, Long charityId);


    // ================= Admin端：系统管理员接口 =================

    /**
     * 分页获取捐赠活动列表（管理员后台专用）
     * @param status 状态过滤：0-待审核，1-已通过，2-已驳回
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     */
    IPage<DonationCampaign> getAdminDonationPage(Integer status, int pageNum, int pageSize);

    /**
     * 审核捐赠活动
     * @param auditDTO 包含活动ID、目标状态和审核备注
     */

    // 修改后 (明确指定为 String)
    Result<String> auditDonationCampaign(AuditDTO auditDTO);
}
