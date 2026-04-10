package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AuditDTO {
    @NotNull(message = "审核对象ID不能为空")
    private Long targetId;
    @NotNull(message = "审核对象类型不能为空")
    private Integer targetType;
    @NotNull(message = "审核操作不能为空")
    private Integer auditAction;
    private String reason;
}
