package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private String coverImg;
    private Integer productType;
    private BigDecimal price;
    private String exchangeDesc;
    private String campus;
    private String contactInfo;
    private Integer quality;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String publisherName;
    @TableField(exist = false)
    private String publisherAvatar;
    @TableField(exist = false)
    private List<ProductImage> images;
    @TableField(exist = false)
    private List<ProductTag> tags;
    @TableField(exist = false)
    private Boolean isFavorited;
}
