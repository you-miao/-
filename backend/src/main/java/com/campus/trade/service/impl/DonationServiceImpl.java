package com.campus.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.*;
import com.campus.trade.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<DonationOrder> getMyDonations(Long userId) {
        QueryWrapper<DonationOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("donor_id", userId).orderByDesc("create_time");
        return donationOrderMapper.selectList(wrapper);
    }

    @Override
    public List<DonationAnnouncement> getAnnouncements() {
        QueryWrapper<DonationAnnouncement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        return donationAnnouncementMapper.selectList(wrapper);
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

        // 1. 插入捐赠订单记录
        donationOrderMapper.insert(order);

        // 2. 💡 重点新增：如果关联了募捐活动，则活动已筹集数量自动 +1
        if (order.getCampaignId() != null) {
            DonationCampaign campaign = donationCampaignMapper.selectById(order.getCampaignId());
            if (campaign != null) {
                // 安全处理：防止由于历史脏数据导致的空指针异常
                int currentCount = campaign.getCurrentCount() == null ? 0 : campaign.getCurrentCount();
                campaign.setCurrentCount(currentCount + 1);
                // 更新回数据库
                donationCampaignMapper.updateById(campaign);
            }
        }
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
    public boolean receiveDonation(Long orderId, Long charityId) {
        DonationOrder order = donationOrderMapper.selectById(orderId);
        if (order != null && order.getStatus() == 0) {
            order.setStatus(1); // 社团已接收
            order.setCharityId(charityId);
            // 💡 返回 update 结果（大于0说明更新成功）
            return donationOrderMapper.updateById(order) > 0;
        }
        return false;
    }

    @Override
    public boolean publishCampaign(DonationCampaign campaign) {
        campaign.setCurrentCount(0); // 初始筹集到的数量为0
        return donationCampaignMapper.insert(campaign) > 0;
    }
}