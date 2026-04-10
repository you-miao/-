package com.campus.trade.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.PaymentRecord;
import com.campus.trade.entity.RechargeRecord;
import com.campus.trade.entity.SysUser;
import com.campus.trade.mapper.PaymentRecordMapper;
import com.campus.trade.mapper.RechargeRecordMapper;
import com.campus.trade.mapper.SysUserMapper;
import com.campus.trade.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletServiceImpl implements WalletService {

    private static final BigDecimal MAX_RECHARGE = new BigDecimal("50000.00");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String fmt(LocalDateTime t) {
        return t == null ? null : t.format(TIME_FMT);
    }

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private RechargeRecordMapper rechargeRecordMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    public BigDecimal getBalance(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal b = u.getBalance();
        return b != null ? b.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RechargeRecord recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(MAX_RECHARGE) > 0) {
            throw new BusinessException("单次充值金额不能超过50000元");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        BigDecimal before = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        int n = userMapper.addBalance(userId, amount);
        if (n == 0) {
            throw new BusinessException("充值失败");
        }
        SysUser afterUser = userMapper.selectById(userId);
        BigDecimal after = afterUser.getBalance() != null ? afterUser.getBalance() : BigDecimal.ZERO;

        RechargeRecord record = new RechargeRecord();
        record.setRechargeNo(IdUtil.getSnowflakeNextIdStr());
        record.setUserId(userId);
        record.setAmount(amount);
        record.setBalanceBefore(before);
        record.setBalanceAfter(after);
        record.setRechargeMethod(1);
        record.setStatus(1);
        record.setRechargeTime(LocalDateTime.now());
        record.setRemark("模拟充值到账");
        rechargeRecordMapper.insert(record);
        return record;
    }

    @Override
    public IPage<RechargeRecord> getRechargeRecords(Long userId, int pageNum, int pageSize) {
        return rechargeRecordMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<RechargeRecord>()
                        .eq(RechargeRecord::getUserId, userId)
                        .orderByDesc(RechargeRecord::getCreateTime));
    }

    @Override
    public IPage<Map<String, Object>> getTransactionRecords(Long userId, int pageNum, int pageSize) {
        List<Map<String, Object>> all = new ArrayList<>();

        List<RechargeRecord> recharges = rechargeRecordMapper.selectList(
                new LambdaQueryWrapper<RechargeRecord>()
                        .eq(RechargeRecord::getUserId, userId)
                        .eq(RechargeRecord::getStatus, 1)
                        .orderByDesc(RechargeRecord::getRechargeTime));
        for (RechargeRecord r : recharges) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "recharge");
            m.put("bizTime", fmt(r.getRechargeTime()));
            m.put("amount", r.getAmount());
            m.put("remark", "余额充值");
            all.add(m);
        }

        List<PaymentRecord> pays = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getUserId, userId)
                        .eq(PaymentRecord::getStatus, 1)
                        .orderByDesc(PaymentRecord::getPayTime));
        for (PaymentRecord p : pays) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "pay");
            m.put("bizTime", fmt(p.getPayTime()));
            m.put("amount", p.getAmount().negate());
            m.put("remark", "订单支付 " + p.getOrderNo());
            all.add(m);
        }

        for (PaymentRecord p : paymentRecordMapper.selectSellerBalanceIncome(userId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", "income");
            m.put("bizTime", fmt(p.getPayTime()));
            m.put("amount", p.getAmount());
            m.put("remark", "售出收款 " + p.getOrderNo());
            all.add(m);
        }

        all.sort(Comparator.comparing((Map<String, Object> m) -> String.valueOf(m.get("bizTime"))).reversed());

        int total = all.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, total);
        List<Map<String, Object>> slice = from >= total ? new ArrayList<>() : new ArrayList<>(all.subList(from, to));

        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(slice);
        return page;
    }
}
