package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.Result;
import com.campus.trade.dto.OrderDTO;
import com.campus.trade.dto.PayOrderDTO;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping
    public Result<TradeOrder> create(@Valid @RequestBody OrderDTO dto) {
        return Result.success(orderService.createOrder(SecurityUtil.getCurrentUserId(), dto));
    }

    @ApiOperation("支付订单（payMethod：1-线下支付 2-平台余额）")
    @PutMapping("/pay/{id}")
    public Result<Void> pay(@PathVariable Long id, @RequestBody(required = false) PayOrderDTO dto) {
        Integer payMethod = dto != null && dto.getPayMethod() != null ? dto.getPayMethod() : 1;
        orderService.payOrder(SecurityUtil.getCurrentUserId(), id, payMethod);
        return Result.success();
    }

    @ApiOperation("确认收货")
    @PutMapping("/receive/{id}")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        orderService.confirmReceive(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        orderService.cancelOrder(SecurityUtil.getCurrentUserId(), id, reason);
        return Result.success();
    }

    @ApiOperation("我的买入订单")
    @GetMapping("/buy")
    public Result<IPage<TradeOrder>> buyOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(orderService.getOrderPage(SecurityUtil.getCurrentUserId(), "buyer", status, pageNum, pageSize));
    }

    @ApiOperation("我的卖出订单")
    @GetMapping("/sell")
    public Result<IPage<TradeOrder>> sellOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(orderService.getOrderPage(SecurityUtil.getCurrentUserId(), "seller", status, pageNum, pageSize));
    }

    @ApiOperation("订单详情")
    @GetMapping("/{id}")
    public Result<TradeOrder> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }
}
