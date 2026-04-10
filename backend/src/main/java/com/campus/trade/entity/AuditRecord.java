package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_record")
public class AuditRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer targetType;
    private Long targetId;
    private Long auditorId;
    private Integer auditAction;
    private String reason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String auditorName;
}
