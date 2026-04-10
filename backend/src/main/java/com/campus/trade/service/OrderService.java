package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.dto.OrderDTO;
import com.campus.trade.entity.TradeOrder;

public interface OrderService {
    TradeOrder createOrder(Long buyerId, OrderDTO dto);
    void payOrder(Long userId, Long orderId, Integer payMethod);
    void confirmReceive(Long userId, Long orderId);
    void cancelOrder(Long userId, Long orderId, String reason);
    IPage<TradeOrder> getOrderPage(Long userId, String role, Integer status, int pageNum, int pageSize);
    TradeOrder getOrderDetail(Long orderId);
}
