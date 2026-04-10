package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.PaymentRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    /** 卖家通过余额支付收到的货款 */
    @Select("SELECT pr.* FROM payment_record pr INNER JOIN trade_order o ON pr.order_id = o.id "
            + "WHERE o.seller_id = #{userId} AND pr.pay_method = 2 AND pr.status = 1")
    List<PaymentRecord> selectSellerBalanceIncome(@Param("userId") Long userId);
}
