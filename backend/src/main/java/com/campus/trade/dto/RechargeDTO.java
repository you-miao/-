package com.campus.trade.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class RechargeDTO {
    @NotNull(message = "请输入充值金额")
    @DecimalMin(value = "0.01", message = "充值金额不少于0.01元")
    private BigDecimal amount;
}
