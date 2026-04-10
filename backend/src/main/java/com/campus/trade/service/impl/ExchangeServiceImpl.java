package com.campus.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.ExchangeDTO;
import com.campus.trade.entity.ExchangeMessage;
import com.campus.trade.entity.ExchangeRequest;
import com.campus.trade.entity.Product;
import com.campus.trade.mapper.ExchangeMessageMapper;
import com.campus.trade.mapper.ExchangeRequestMapper;
import com.campus.trade.mapper.ProductMapper;
import com.campus.trade.service.ExchangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Resource
    private ExchangeRequestMapper exchangeMapper;
    @Resource
    private ExchangeMessageMapper messageMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void applyExchange(Long applicantId, ExchangeDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getProductType() != 2) {
            throw new BusinessException("该商品为售卖物，请直接购买");
        }
        if (product.getUserId().equals(applicantId)) {
            throw new BusinessException("不能与自己交换");
        }

        Long acceptedCount = exchangeMapper.selectCount(
                new LambdaQueryWrapper<ExchangeRequest>()
                        .eq(ExchangeRequest::getProductId, dto.getProductId())
                        .eq(ExchangeRequest::getStatus, 1));
        if (acceptedCount > 0) {
            throw new BusinessException("该商品已有进行中的交换，暂不可申请");
        }

        if (dto.getOfferProductId() != null) {
            Product offerProduct = productMapper.selectById(dto.getOfferProductId());
            if (offerProduct == null || !offerProduct.getUserId().equals(applicantId)) {
                throw new BusinessException("交换物品不存在或不属于你");
            }
            if (offerProduct.getStatus() != 1) {
                throw new BusinessException("交换物品未上架，无法用于交换");
            }
        }

        ExchangeRequest request = new ExchangeRequest();
        request.setRequestNo(IdUtil.getSnowflakeNextIdStr());
        request.setProductId(product.getId());
        request.setApplicantId(applicantId);
        request.setOwnerId(product.getUserId());
        request.setOfferProductId(dto.getOfferProductId());
        request.setOfferDesc(dto.getOfferDesc());
        request.setMessage(dto.getMessage());
        request.setStatus(0);
        if (dto.getOfferImages() != null) {
            try {
                request.setOfferImages(objectMapper.writeValueAsString(dto.getOfferImages()));
            } catch (JsonProcessingException ignored) {}
        }
        exchangeMapper.insert(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptExchange(Long ownerId, Long exchangeId) {
        ExchangeRequest request = exchangeMapper.selectById(exchangeId);
        if (request == null || !request.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权操作");
        }
        if (request.getStatus() != 0) {
            throw new BusinessException("申请状态不正确");
        }

        // 检查该商品是否已有其他已同意的申请
        Long acceptedCount = exchangeMapper.selectCount(
                new LambdaQueryWrapper<ExchangeRequest>()
                        .eq(ExchangeRequest::getProductId, request.getProductId())
                        .eq(ExchangeRequest::getStatus, 1));
        if (acceptedCount > 0) {
            throw new BusinessException("该商品已有进行中的交换，无法重复同意");
        }

        request.setStatus(1);
        exchangeMapper.updateById(request);

        // 自动拒绝同一商品的其他待处理申请
        List<ExchangeRequest> pendingList = exchangeMapper.selectList(
                new LambdaQueryWrapper<ExchangeRequest>()
                        .eq(ExchangeRequest::getProductId, request.getProductId())
                        .eq(ExchangeRequest::getStatus, 0)
                        .ne(ExchangeRequest::getId, exchangeId));
        for (ExchangeRequest pending : pendingList) {
            pending.setStatus(2);
            pending.setRejectReason("物主已接受其他交换申请");
            exchangeMapper.updateById(pending);
        }
    }

    @Override
    public void rejectExchange(Long ownerId, Long exchangeId, String reason) {
        ExchangeRequest request = exchangeMapper.selectById(exchangeId);
        if (request == null || !request.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权操作");
        }
        request.setStatus(2);
        request.setRejectReason(reason);
        exchangeMapper.updateById(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmComplete(Long userId, Long exchangeId) {
        ExchangeRequest request = exchangeMapper.selectById(exchangeId);
        if (request == null || request.getStatus() != 1) {
            throw new BusinessException("交换状态不正确");
        }
        if (userId.equals(request.getApplicantId())) {
            request.setApplicantConfirm(1);
        } else if (userId.equals(request.getOwnerId())) {
            request.setOwnerConfirm(1);
        } else {
            throw new BusinessException("无权操作");
        }

        if (request.getApplicantConfirm() == 1 && request.getOwnerConfirm() == 1) {
            request.setStatus(3);
            // 目标商品标记为已交换
            Product product = productMapper.selectById(request.getProductId());
            product.setStatus(3);
            productMapper.updateById(product);
            // 交换物商品也标记为已交换
            if (request.getOfferProductId() != null) {
                Product offerProduct = productMapper.selectById(request.getOfferProductId());
                if (offerProduct != null) {
                    offerProduct.setStatus(3);
                    productMapper.updateById(offerProduct);
                }
            }
        }
        exchangeMapper.updateById(request);
    }

    @Override
    public void cancelExchange(Long userId, Long exchangeId) {
        ExchangeRequest request = exchangeMapper.selectById(exchangeId);
        if (request == null || !request.getApplicantId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (request.getStatus() > 1) {
            throw new BusinessException("当前状态不可取消");
        }
        request.setStatus(4);
        exchangeMapper.updateById(request);
    }

    @Override
    public IPage<ExchangeRequest> getExchangePage(Long userId, String role, Integer status, int pageNum, int pageSize) {
        return exchangeMapper.selectExchangePage(new Page<>(pageNum, pageSize), userId, role, status);
    }

    @Override
    public ExchangeRequest getExchangeDetail(Long exchangeId) {
        return exchangeMapper.selectById(exchangeId);
    }

    @Override
    public void sendMessage(Long senderId, Long exchangeId, String content, Integer msgType) {
        ExchangeRequest request = exchangeMapper.selectById(exchangeId);
        if (request == null) {
            throw new BusinessException("交换申请不存在");
        }
        Long receiverId = senderId.equals(request.getApplicantId()) ? request.getOwnerId() : request.getApplicantId();
        ExchangeMessage msg = new ExchangeMessage();
        msg.setExchangeId(exchangeId);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setMsgType(msgType != null ? msgType : 1);
        msg.setIsRead(0);
        messageMapper.insert(msg);
    }

    @Override
    public List<ExchangeMessage> getMessages(Long exchangeId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ExchangeMessage>()
                        .eq(ExchangeMessage::getExchangeId, exchangeId)
                        .orderByAsc(ExchangeMessage::getCreateTime));
    }

    @Override
    public List<Product> getMyExchangeProducts(Long userId) {
        Page<Product> page = new Page<>(1, 200);
        IPage<Product> result = productMapper.selectProductPage(page, null, null, null, null, null, 1, userId, null, null);
        return result.getRecords();
    }
}
