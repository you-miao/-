package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("donation_campaign")
public class DonationCampaign {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long charityId;

    private String title;

    private String description;

    private String coverImg;

    private Integer targetCount;

    private Integer currentCount;

    private Integer status; // 0-待审核 1-进行中 2-已结束 3-已驳回

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}