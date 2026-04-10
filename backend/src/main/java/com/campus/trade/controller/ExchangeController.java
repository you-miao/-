package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.Result;
import com.campus.trade.dto.ExchangeDTO;
import com.campus.trade.entity.ExchangeMessage;
import com.campus.trade.entity.ExchangeRequest;
import com.campus.trade.entity.Product;
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.ExchangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "交换接口")
@RestController
@RequestMapping("/exchange")
public class ExchangeController {

    @Resource
    private ExchangeService exchangeService;

    @ApiOperation("发起交换申请")
    @PostMapping
    public Result<Void> apply(@Valid @RequestBody ExchangeDTO dto) {
        exchangeService.applyExchange(SecurityUtil.getCurrentUserId(), dto);
        return Result.ok("申请已提交");
    }

    @ApiOperation("同意交换")
    @PutMapping("/accept/{id}")
    public Result<Void> accept(@PathVariable Long id) {
        exchangeService.acceptExchange(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("拒绝交换")
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        exchangeService.rejectExchange(SecurityUtil.getCurrentUserId(), id, reason);
        return Result.success();
    }

    @ApiOperation("确认交换完成")
    @PutMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id) {
        exchangeService.confirmComplete(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("取消交换")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        exchangeService.cancelExchange(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("我发起的交换")
    @GetMapping("/my/apply")
    public Result<IPage<ExchangeRequest>> myApply(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(exchangeService.getExchangePage(SecurityUtil.getCurrentUserId(), "applicant", status, pageNum, pageSize));
    }

    @ApiOperation("收到的交换申请")
    @GetMapping("/my/receive")
    public Result<IPage<ExchangeRequest>> myReceive(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(exchangeService.getExchangePage(SecurityUtil.getCurrentUserId(), "owner", status, pageNum, pageSize));
    }

    @ApiOperation("交换详情")
    @GetMapping("/{id}")
    public Result<ExchangeRequest> detail(@PathVariable Long id) {
        return Result.success(exchangeService.getExchangeDetail(id));
    }

    @ApiOperation("发送沟通消息")
    @PostMapping("/message/{exchangeId}")
    public Result<Void> sendMessage(@PathVariable Long exchangeId,
                                     @RequestParam String content,
                                     @RequestParam(required = false) Integer msgType) {
        exchangeService.sendMessage(SecurityUtil.getCurrentUserId(), exchangeId, content, msgType);
        return Result.success();
    }

    @ApiOperation("获取沟通消息列表")
    @GetMapping("/message/{exchangeId}")
    public Result<List<ExchangeMessage>> getMessages(@PathVariable Long exchangeId) {
        return Result.success(exchangeService.getMessages(exchangeId));
    }

    @ApiOperation("获取我的可交换商品列表")
    @GetMapping("/my/products")
    public Result<List<Product>> myExchangeProducts() {
        return Result.success(exchangeService.getMyExchangeProducts(SecurityUtil.getCurrentUserId()));
    }
}
