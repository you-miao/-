package com.campus.trade.dto;

import lombok.Data;

/**
 * 支付订单：1-线下支付（记账） 2-平台余额
 */
@Data
public class PayOrderDTO {
    private Integer payMethod;
}
