package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_record")
public class RechargeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String rechargeNo;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    /** 1-模拟充值 */
    private Integer rechargeMethod;
    /** 0-处理中 1-成功 2-失败 */
    private Integer status;
    private LocalDateTime rechargeTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
