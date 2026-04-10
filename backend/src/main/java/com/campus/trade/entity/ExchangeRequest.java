package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exchange_request")
public class ExchangeRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestNo;
    private Long productId;
    private Long applicantId;
    private Long ownerId;
    private Long offerProductId;
    private String offerDesc;
    private String offerImages;
    private String message;
    private String exchangeLocation;
    private LocalDateTime exchangeTime;
    private Integer status;
    private String rejectReason;
    private Integer applicantConfirm;
    private Integer ownerConfirm;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productTitle;
    @TableField(exist = false)
    private String productCoverImg;
    @TableField(exist = false)
    private String applicantName;
    @TableField(exist = false)
    private String ownerName;
    @TableField(exist = false)
    private String offerProductTitle;
    @TableField(exist = false)
    private String offerProductCoverImg;
    @TableField(exist = false)
    private String offerProductCategoryName;
    @TableField(exist = false)
    private java.math.BigDecimal offerProductPrice;
}
