package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.ExchangeRequest;
import org.apache.ibatis.annotations.Param;

public interface ExchangeRequestMapper extends BaseMapper<ExchangeRequest> {

    IPage<ExchangeRequest> selectExchangePage(Page<ExchangeRequest> page,
                                               @Param("userId") Long userId,
                                               @Param("role") String role,
                                               @Param("status") Integer status);
}
