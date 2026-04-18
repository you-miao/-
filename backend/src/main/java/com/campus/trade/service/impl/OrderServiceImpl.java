package com.campus.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.OrderDTO;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.*;
import com.campus.trade.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private TradeOrderMapper orderMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private UserAddressMapper addressMapper;
    @Resource
    private PaymentRecordMapper paymentMapper;
    @Resource
    private SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(Long buyerId, OrderDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getProductType() != 1) {
            throw new BusinessException("该商品为交换物，不可直接购买");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }
        Long activeOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getProductId, product.getId())
                .in(TradeOrder::getStatus, 0, 1, 2));
        if (activeOrderCount > 0) {
            throw new BusinessException("该商品已被下单，请选择其他商品");
        }

        TradeOrder order = new TradeOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setProductId(product.getId());
        order.setSellerId(product.getUserId());
        order.setBuyerId(buyerId);
        order.setPrice(product.getPrice());
        order.setDeliveryType(dto.getDeliveryType() != null ? dto.getDeliveryType() : 1);
        order.setStatus(0);
        order.setContactInfo(dto.getContactInfo());
        order.setRemark(dto.getRemark());

        if (dto.getDeliveryType() != null && dto.getDeliveryType() == 2 && dto.getAddressId() != null) {
            UserAddress addr = addressMapper.selectById(dto.getAddressId());
            if (addr != null) {
                order.setAddressId(addr.getId());
                order.setReceiverName(addr.getReceiver());
                order.setReceiverPhone(addr.getPhone());
                order.setReceiverAddress(addr.getCampus() + " " + addr.getDetail());
            }
        } else {
            order.setPickupLocation(dto.getPickupLocation());
        }

        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId, Integer payMethod) {
        if (payMethod == null) {
            payMethod = 1;
        }
        if (payMethod != 1 && payMethod != 2) {
            throw new BusinessException("不支持的支付方式");
        }
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }
        Product product = productMapper.selectById(order.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品状态已变化，无法继续支付");
        }
        Long paidOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getProductId, order.getProductId())
                .in(TradeOrder::getStatus, 1, 2));
        if (paidOrderCount > 0) {
            throw new BusinessException("该商品已被他人购买");
        }

        BigDecimal price = order.getPrice();
        if (payMethod == 2) {
            int ok = userMapper.deductBalance(userId, price);
            if (ok == 0) {
                throw new BusinessException("余额不足");
            }
            int addOk = userMapper.addBalance(order.getSellerId(), price);
            if (addOk == 0) {
                throw new BusinessException("支付失败，请稍后重试");
            }
        }

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        PaymentRecord payment = new PaymentRecord();
        payment.setPaymentNo(IdUtil.getSnowflakeNextIdStr());
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(price);
        payment.setPayMethod(payMethod);
        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        product.setStatus(4);
        productMapper.updateById(product);
    }

    @Override
    public void confirmReceive(Long userId, Long orderId) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }
        order.setStatus(2);
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public void cancelOrder(Long userId, Long orderId, String reason) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getBuyerId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待付款订单可取消");
        }
        order.setStatus(3);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderMapper.updateById(order);

        Product product = productMapper.selectById(order.getProductId());
        if (product.getStatus() == 4) {
            product.setStatus(1);
            productMapper.updateById(product);
        }
    }

    @Override
    public IPage<TradeOrder> getOrderPage(Long userId, String role, Integer status, int pageNum, int pageSize) {
        return orderMapper.selectOrderPage(new Page<>(pageNum, pageSize), userId, role, status);
    }

    @Override
    public TradeOrder getOrderDetail(Long orderId) {
        return orderMapper.selectOrderDetail(orderId);
    }
}
