package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.dto.ExchangeDTO;
import com.campus.trade.entity.ExchangeMessage;
import com.campus.trade.entity.ExchangeRequest;
import com.campus.trade.entity.Product;

import java.util.List;

public interface ExchangeService {
    void applyExchange(Long applicantId, ExchangeDTO dto);
    void acceptExchange(Long ownerId, Long exchangeId);
    void rejectExchange(Long ownerId, Long exchangeId, String reason);
    void confirmComplete(Long userId, Long exchangeId);
    void cancelExchange(Long userId, Long exchangeId);
    IPage<ExchangeRequest> getExchangePage(Long userId, String role, Integer status, int pageNum, int pageSize);
    ExchangeRequest getExchangeDetail(Long exchangeId);
    void sendMessage(Long senderId, Long exchangeId, String content, Integer msgType);
    List<ExchangeMessage> getMessages(Long exchangeId);
    List<Product> getMyExchangeProducts(Long userId);
}
