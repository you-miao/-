package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.TradeOrder;
import org.apache.ibatis.annotations.Param;

public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    IPage<TradeOrder> selectOrderPage(Page<TradeOrder> page,
                                       @Param("userId") Long userId,
                                       @Param("role") String role,
                                       @Param("status") Integer status);

    TradeOrder selectOrderDetail(@Param("id") Long id);
}
