package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_tag")
public class ProductTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String color;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
