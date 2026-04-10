package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("product_tag_relation")
public class ProductTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long tagId;
}
