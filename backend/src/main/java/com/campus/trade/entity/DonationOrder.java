package com.campus.trade.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("donation_order")
public class DonationOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long donorId;
    private Long campaignId;
    private Long productId;
    private Long charityId;
    private String itemName;
    private String itemImages;
    private String donorName;
    private String donorPhone;
    private String donorAddress;
    private Integer donationType; // 1-定向募捐 2-自愿捐赠
    private Integer status; // 0-待取件 1-社团已接收 2-已发往受助区 3-已公示 4-已取消
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}