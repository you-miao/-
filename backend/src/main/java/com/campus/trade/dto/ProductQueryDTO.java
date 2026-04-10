package com.campus.trade.dto;

import com.campus.trade.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQueryDTO extends PageQuery {
    private Long categoryId;
    private Long tagId;
    private Integer productType;
    private String campus;
    private String keyword;
    private Integer status;
    private Long userId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
