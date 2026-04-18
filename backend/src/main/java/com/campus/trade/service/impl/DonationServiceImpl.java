package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trade.common.Result;
import com.campus.trade.dto.AuditDTO;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.*;
import com.campus.trade.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DonationServiceImpl implements DonationService {

    @Autowired private DonationOrderMapper donationOrderMapper;
    @Autowired private DonationCampaignMapper donationCampaignMapper;
    @Autowired private DonationAnnouncementMapper donationAnnouncementMapper;
    @Autowired private ProductMapper productMapper;

    // ================= C端：普通用户接口 =================

    @Override
    public List<DonationCampaign> getActiveCampaigns() {
        QueryWrapper<DonationCampaign> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        return donationCampaignMapper.selectList(wrapper);
    }

    @Override
    public List<DonationAnnouncement> getAnnouncements() {
        QueryWrapper<DonationAnnouncement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        return donationAnnouncementMapper.selectList(wrapper);
    }

    @Override
    public List<DonationOrder> getMyDonations(Long userId) {
        QueryWrapper<DonationOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("donor_id", userId).orderByDesc("create_time");
        List<DonationOrder> orders = donationOrderMapper.selectList(wrapper);
        List<Long> campaignIds = orders.stream()
                .map(DonationOrder::getCampaignId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (!campaignIds.isEmpty()) {
            Map<Long, String> titleMap = new HashMap<>();
            donationCampaignMapper.selectBatchIds(campaignIds)
                    .forEach(c -> titleMap.put(c.getId(), c.getTitle()));
            orders.forEach(o -> o.setCampaignTitle(titleMap.get(o.getCampaignId())));
        }
        return orders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitDonationOrder(DonationOrder order, Long currentUserId) {
        order.setDonorId(currentUserId);
        order.setOrderNo("DON" + IdUtil.getSnowflakeNextIdStr());
        order.setStatus(0); // 待取件

        // 一键转捐赠逻辑
        if (order.getProductId() != null) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null && product.getUserId().equals(currentUserId)) {
                product.setStatus(6); // 修改商品状态为：已捐赠
                productMapper.updateById(product);
                if (order.getItemName() == null) order.setItemName(product.getTitle());
                if (order.getItemImages() == null) order.setItemImages(product.getCoverImg());
            } else {
                throw new RuntimeException("非法的商品操作");
            }
        }

        // 插入捐赠订单记录，活动数量在“接收订单”时再累加，避免虚高
        donationOrderMapper.insert(order);
    }

    // ================= B端：社团管理端接口 =================

    @Override
    public List<DonationOrder> getAllOrders() {
        // 获取所有订单，按时间倒序排
        QueryWrapper<DonationOrder> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return donationOrderMapper.selectList(wrapper);
    }

    @Override
    public List<DonationCampaign> getCharityCampaigns(Long charityId) {
        QueryWrapper<DonationCampaign> wrapper = new QueryWrapper<>();
        // 社团工作台展示全部活动，兼容历史数据归属到其他账号的情况
        wrapper.orderByDesc("create_time");
        return donationCampaignMapper.selectList(wrapper);
    }

    @Override
    public List<DonationOrder> getCampaignOrders(Long campaignId, Integer status) {
        QueryWrapper<DonationOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("campaign_id", campaignId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return donationOrderMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveDonation(Long orderId, Long charityId) {
        DonationOrder order = donationOrderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(1); // 社团已接收
            order.setCharityId(charityId);
            boolean updated = donationOrderMapper.updateById(order) > 0;
            if (!updated) {
                return false;
            }

            // 只有接收成功后才累加活动数量
            if (order.getCampaignId() != null) {
                DonationCampaign campaign = donationCampaignMapper.selectById(order.getCampaignId());
                if (campaign != null) {
                    int currentCount = campaign.getCurrentCount() == null ? 0 : campaign.getCurrentCount();
                    campaign.setCurrentCount(currentCount + 1);
                    donationCampaignMapper.updateById(campaign);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean rejectDonation(Long orderId, Long charityId, String reason) {
        DonationOrder order = donationOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setStatus(4); // 已取消/已驳回
        order.setCharityId(charityId);
        order.setRemark(reason);
        return donationOrderMapper.updateById(order) > 0;
    }

    @Override
    public boolean publishCampaign(DonationCampaign campaign) {
        campaign.setCurrentCount(0); // 初始筹集到的数量为0

        // 💡 自动帮你加上的修复代码：发布时赋默认状态值为 0 (待审核)
        campaign.setStatus(0);

        return donationCampaignMapper.insert(campaign) > 0;
    }

    @Override
    public boolean offlineCampaign(Long campaignId, Long charityId) {
        DonationCampaign campaign = donationCampaignMapper.selectById(campaignId);
        if (campaign == null || !charityId.equals(campaign.getCharityId())) {
            return false;
        }
        campaign.setStatus(3); // 社团主动下架/结束
        return donationCampaignMapper.updateById(campaign) > 0;
    }

    @Override
    public boolean adjustCampaignTarget(Long campaignId, Long charityId, Integer delta) {
        if (delta == null || delta == 0) {
            return false;
        }
        DonationCampaign campaign = donationCampaignMapper.selectById(campaignId);
        if (campaign == null || !charityId.equals(campaign.getCharityId())) {
            return false;
        }
        int base = campaign.getTargetCount() == null ? 0 : campaign.getTargetCount();
        int next = base + delta;
        if (next < 0) {
            next = 0;
        }
        campaign.setTargetCount(next);
        return donationCampaignMapper.updateById(campaign) > 0;
    }

    @Override
    public boolean publishAnnouncement(DonationAnnouncement announcement, Long charityId) {
        announcement.setCharityId(charityId);
        announcement.setStatus(1);
        return donationAnnouncementMapper.insert(announcement) > 0;
    }

    // ================= Admin端：系统管理员接口 =================

    @Override
    public IPage<DonationCampaign> getAdminDonationPage(Integer status, int pageNum, int pageSize) {
        Page<DonationCampaign> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DonationCampaign> wrapper = new QueryWrapper<>();

        // 如果前端传了 status (比如 0 代表待审核)，就加上这个查询条件
        if (status != null) {
            wrapper.eq("status", status);
        }
        // 按发布时间倒序排列
        wrapper.orderByDesc("create_time");

        return donationCampaignMapper.selectPage(page, wrapper);
    }

    @Override
    public Result<String> auditDonationCampaign(AuditDTO auditDTO) {
        // 使用 getTargetId()
        DonationCampaign campaign = donationCampaignMapper.selectById(auditDTO.getTargetId());
        if (campaign == null) {
            return Result.error("捐赠活动不存在");
        }

        // 使用 getAuditAction()
        campaign.setStatus(auditDTO.getAuditAction());

        // 执行更新操作
        boolean updated = donationCampaignMapper.updateById(campaign) > 0;
        if (updated) {
            return Result.ok("审核操作成功");
        } else {
            return Result.error("审核操作失败");
        }
    }
}
