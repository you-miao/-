package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment_report")
public class CommentReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long reporterId;
    private String reason;
    private Integer status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime handleTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
