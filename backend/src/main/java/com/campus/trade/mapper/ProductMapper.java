package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.Product;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper extends BaseMapper<Product> {

    IPage<Product> selectProductPage(Page<Product> page,
                                     @Param("categoryId") Long categoryId,
                                     @Param("tagId") Long tagId,
                                     @Param("productType") Integer productType,
                                     @Param("campus") String campus,
                                     @Param("keyword") String keyword,
                                     @Param("status") Integer status,
                                     @Param("userId") Long userId,
                                     @Param("minPrice") java.math.BigDecimal minPrice,
                                     @Param("maxPrice") java.math.BigDecimal maxPrice);

    IPage<Product> selectRecommendProducts(Page<Product> page,
                                           @Param("userId") Long userId,
                                           @Param("excludeUserId") boolean excludeUserId);

    Product selectProductDetail(@Param("id") Long id);
}
