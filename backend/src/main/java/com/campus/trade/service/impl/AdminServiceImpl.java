package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.AuditDTO;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.*;
import com.campus.trade.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AuditRecordMapper auditMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private TradeOrderMapper orderMapper;
    @Resource
    private ExchangeRequestMapper exchangeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long auditorId, AuditDTO dto) {
        AuditRecord record = new AuditRecord();
        record.setTargetType(dto.getTargetType());
        record.setTargetId(dto.getTargetId());
        record.setAuditorId(auditorId);
        record.setAuditAction(dto.getAuditAction());
        record.setReason(dto.getReason());
        auditMapper.insert(record);

        if (dto.getTargetType() == 1) {
            Product product = productMapper.selectById(dto.getTargetId());
            if (product == null) throw new BusinessException("商品不存在");
            switch (dto.getAuditAction()) {
                case 1: product.setStatus(1); break;
                case 2: product.setStatus(5); break;
                case 3: product.setStatus(2); break;
                default: break;
            }
            productMapper.updateById(product);
        } else if (dto.getTargetType() == 2) {
            Comment comment = commentMapper.selectById(dto.getTargetId());
            if (comment == null) throw new BusinessException("评论不存在");
            switch (dto.getAuditAction()) {
                case 1: comment.setStatus(1); break;
                case 2: comment.setStatus(2); break;
                case 4: commentMapper.deleteById(dto.getTargetId()); return;
                default: break;
            }
            commentMapper.updateById(comment);
        }
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalProducts", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getDeleted, 0)));
        stats.put("totalOrders", orderMapper.selectCount(null));
        stats.put("totalExchanges", exchangeMapper.selectCount(
                new LambdaQueryWrapper<ExchangeRequest>().eq(ExchangeRequest::getStatus, 3)));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.put("todayNewUsers", userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().ge(SysUser::getCreateTime, todayStart)));
        stats.put("todayNewProducts", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().ge(Product::getCreateTime, todayStart)));
        stats.put("pendingAuditProducts", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 0).eq(Product::getDeleted, 0)));
        stats.put("pendingAuditComments", commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0)));

        return stats;
    }
}
