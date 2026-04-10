package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.dto.ProductDTO;
import com.campus.trade.dto.ProductQueryDTO;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.ProductCategory;
import com.campus.trade.entity.ProductTag;

import java.util.List;

public interface ProductService {
    void publishProduct(Long userId, ProductDTO dto);
    void updateProduct(Long userId, ProductDTO dto);
    IPage<Product> getProductPage(ProductQueryDTO query);
    IPage<Product> getRecommendProducts(Long userId, int pageNum, int pageSize);
    Product getProductDetail(Long id, Long currentUserId);
    void offShelf(Long userId, Long productId);
    void toggleFavorite(Long userId, Long productId);
    IPage<Product> getMyFavorites(Long userId, int pageNum, int pageSize);
    List<ProductCategory> getCategoryTree();
    List<ProductTag> getAllTags();
}
