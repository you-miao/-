package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ExchangeDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    private Long offerProductId;
    private String offerDesc;
    private List<String> offerImages;
    private String message;
}
