package com.campus.trade.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("donation_announcement")
public class DonationAnnouncement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long charityId;
    private Long campaignId;
    private String title;
    private String recipientInfo;
    private String content;
    private String proofImages;
    private Integer status; // 0-隐藏 1-正常显示
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}