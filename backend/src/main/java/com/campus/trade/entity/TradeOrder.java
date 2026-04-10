package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private BigDecimal price;
    private Integer deliveryType;
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String pickupLocation;
    private LocalDateTime pickupTime;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime receiveTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String contactInfo;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productTitle;
    @TableField(exist = false)
    private String productCoverImg;
    @TableField(exist = false)
    private String sellerName;
    @TableField(exist = false)
    private String buyerName;
}
