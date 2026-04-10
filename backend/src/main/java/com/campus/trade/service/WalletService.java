package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.entity.RechargeRecord;

import java.math.BigDecimal;
import java.util.Map;

public interface WalletService {
    BigDecimal getBalance(Long userId);
    RechargeRecord recharge(Long userId, BigDecimal amount);
    IPage<RechargeRecord> getRechargeRecords(Long userId, int pageNum, int pageSize);
    IPage<Map<String, Object>> getTransactionRecords(Long userId, int pageNum, int pageSize);
}
