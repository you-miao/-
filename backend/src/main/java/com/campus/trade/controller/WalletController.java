package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.Result;
import com.campus.trade.dto.RechargeDTO;
import com.campus.trade.entity.RechargeRecord;
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

@Api(tags = "钱包与充值")
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Resource
    private WalletService walletService;

    @ApiOperation("当前余额")
    @GetMapping("/balance")
    public Result<BigDecimal> balance() {
        return Result.success(walletService.getBalance(SecurityUtil.getCurrentUserId()));
    }

    @ApiOperation("模拟充值（演示环境直接到账）")
    @PostMapping("/recharge")
    public Result<RechargeRecord> recharge(@Valid @RequestBody RechargeDTO dto) {
        return Result.success(walletService.recharge(SecurityUtil.getCurrentUserId(), dto.getAmount()));
    }

    @ApiOperation("充值记录分页")
    @GetMapping("/recharge-records")
    public Result<IPage<RechargeRecord>> rechargeRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(walletService.getRechargeRecords(SecurityUtil.getCurrentUserId(), pageNum, pageSize));
    }

    @ApiOperation("资金流水（充值、订单支出、余额售出收款）")
    @GetMapping("/transactions")
    public Result<IPage<Map<String, Object>>> transactions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(walletService.getTransactionRecords(SecurityUtil.getCurrentUserId(), pageNum, pageSize));
    }
}
