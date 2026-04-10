package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;
    private Integer deliveryType;
    private Long addressId;
    private String pickupLocation;
    private String pickupTime;
    private String remark;
}
