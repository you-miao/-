package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    @NotBlank(message = "商品标题不能为空")
    private String title;
    private String description;
    private Long categoryId;
    @NotNull(message = "商品类型不能为空")
    private Integer productType;
    private BigDecimal price;
    private String exchangeDesc;
    private String campus;
    private String contactInfo;
    private Integer quality;
    private List<String> imageUrls;
    private List<Long> tagIds;
}
