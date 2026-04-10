package com.campus.trade.common;

import lombok.Data;

@Data
public class PageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String orderBy;
    private Boolean isAsc = false;
}
