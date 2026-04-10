package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.ProductDTO;
import com.campus.trade.dto.ProductQueryDTO;
import com.campus.trade.entity.*;
import com.campus.trade.mapper.*;
import com.campus.trade.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;
    @Resource
    private ProductImageMapper imageMapper;
    @Resource
    private ProductTagRelationMapper tagRelationMapper;
    @Resource
    private ProductCategoryMapper categoryMapper;
    @Resource
    private ProductTagMapper tagMapper;
    @Resource
    private UserFavoriteMapper favoriteMapper;
    @Resource
    private UserBrowseLogMapper browseLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishProduct(Long userId, ProductDTO dto) {
        Product product = new Product();
        product.setUserId(userId);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setProductType(dto.getProductType());
        product.setPrice(dto.getPrice());
        product.setExchangeDesc(dto.getExchangeDesc());
        product.setCampus(dto.getCampus());
        product.setContactInfo(dto.getContactInfo());
        product.setQuality(dto.getQuality());
        product.setStatus(0);
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            product.setCoverImg(dto.getImageUrls().get(0));
        }
        productMapper.insert(product);

        saveImages(product.getId(), dto.getImageUrls());
        saveTags(product.getId(), dto.getTagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long userId, ProductDTO dto) {
        Product existing = productMapper.selectById(dto.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此商品");
        }
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setCategoryId(dto.getCategoryId());
        existing.setProductType(dto.getProductType());
        existing.setPrice(dto.getPrice());
        existing.setExchangeDesc(dto.getExchangeDesc());
        existing.setCampus(dto.getCampus());
        existing.setContactInfo(dto.getContactInfo());
        existing.setQuality(dto.getQuality());
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            existing.setCoverImg(dto.getImageUrls().get(0));
        }
        existing.setStatus(0);
        productMapper.updateById(existing);

        imageMapper.delete(new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, dto.getId()));
        tagRelationMapper.delete(new LambdaQueryWrapper<ProductTagRelation>().eq(ProductTagRelation::getProductId, dto.getId()));
        saveImages(dto.getId(), dto.getImageUrls());
        saveTags(dto.getId(), dto.getTagIds());
    }

    @Override
    public IPage<Product> getProductPage(ProductQueryDTO query) {
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<Product> result = productMapper.selectProductPage(page,
                query.getCategoryId(), query.getTagId(), query.getProductType(), query.getCampus(),
                query.getKeyword(), query.getStatus(), query.getUserId(),
                query.getMinPrice(), query.getMaxPrice());
        return result;
    }

    @Override
    public IPage<Product> getRecommendProducts(Long userId, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        IPage<Product> result = productMapper.selectRecommendProducts(page, userId, userId != null);
        if (result.getRecords().isEmpty()) {
            ProductQueryDTO fallback = new ProductQueryDTO();
            fallback.setPageNum(pageNum);
            fallback.setPageSize(pageSize);
            fallback.setStatus(1);
            return getProductPage(fallback);
        }
        return result;
    }

    @Override
    public Product getProductDetail(Long id, Long currentUserId) {
        Product product = productMapper.selectProductDetail(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        List<ProductImage> images = imageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSortOrder));
        product.setImages(images);

        List<ProductTagRelation> relations = tagRelationMapper.selectList(
                new LambdaQueryWrapper<ProductTagRelation>()
                        .eq(ProductTagRelation::getProductId, id));
        if (!relations.isEmpty()) {
            List<Long> tagIds = relations.stream().map(ProductTagRelation::getTagId).collect(Collectors.toList());
            List<ProductTag> tags = tagMapper.selectBatchIds(tagIds);
            product.setTags(tags);
        }

        if (currentUserId != null) {
            Long favCount = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<UserFavorite>()
                            .eq(UserFavorite::getUserId, currentUserId)
                            .eq(UserFavorite::getProductId, id));
            product.setIsFavorited(favCount > 0);
        }

        productMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, id)
                        .setSql("view_count = view_count + 1"));

        if (currentUserId != null) {
            UserBrowseLog log = new UserBrowseLog();
            log.setUserId(currentUserId);
            log.setProductId(id);
            browseLogMapper.insert(log);
        }

        return product;
    }

    @Override
    public void offShelf(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }
        product.setStatus(2);
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getProductId, productId);
        Long count = favoriteMapper.selectCount(wrapper);
        if (count > 0) {
            favoriteMapper.delete(wrapper);
            productMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                            .eq(Product::getId, productId)
                            .setSql("favorite_count = favorite_count - 1"));
        } else {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
            productMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                            .eq(Product::getId, productId)
                            .setSql("favorite_count = favorite_count + 1"));
        }
    }

    @Override
    public IPage<Product> getMyFavorites(Long userId, int pageNum, int pageSize) {
        Page<UserFavorite> favPage = new Page<>(pageNum, pageSize);
        IPage<UserFavorite> favResult = favoriteMapper.selectPage(favPage,
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreateTime));

        List<Long> productIds = favResult.getRecords().stream()
                .map(UserFavorite::getProductId).collect(Collectors.toList());

        Page<Product> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(favResult.getTotal());
        if (productIds.isEmpty()) {
            resultPage.setRecords(new ArrayList<>());
        } else {
            List<Product> products = productMapper.selectBatchIds(productIds);
            resultPage.setRecords(products);
        }
        return resultPage;
    }

    @Override
    public List<ProductCategory> getCategoryTree() {
        List<ProductCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder));
        Map<Long, List<ProductCategory>> grouped = all.stream()
                .filter(c -> c.getParentId() != 0)
                .collect(Collectors.groupingBy(ProductCategory::getParentId));
        return all.stream()
                .filter(c -> c.getParentId() == 0)
                .peek(c -> c.setChildren(grouped.getOrDefault(c.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductTag> getAllTags() {
        return tagMapper.selectList(null);
    }

    private void saveImages(Long productId, List<String> imageUrls) {
        if (imageUrls == null) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage img = new ProductImage();
            img.setProductId(productId);
            img.setUrl(imageUrls.get(i));
            img.setSortOrder(i);
            imageMapper.insert(img);
        }
    }

    private void saveTags(Long productId, List<Long> tagIds) {
        if (tagIds == null) return;
        for (Long tagId : tagIds) {
            ProductTagRelation rel = new ProductTagRelation();
            rel.setProductId(productId);
            rel.setTagId(tagId);
            tagRelationMapper.insert(rel);
        }
    }
}
