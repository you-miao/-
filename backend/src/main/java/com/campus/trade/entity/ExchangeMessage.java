package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exchange_message")
public class ExchangeMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long exchangeId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer msgType;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String senderName;
    @TableField(exist = false)
    private String senderAvatar;
}
