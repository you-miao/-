package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.Result;
import com.campus.trade.dto.ProductDTO;
import com.campus.trade.dto.ProductQueryDTO;
import com.campus.trade.entity.Product;
import com.campus.trade.entity.ProductCategory;
import com.campus.trade.entity.ProductTag;
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "商品接口")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @ApiOperation("发布商品")
    @PostMapping
    public Result<Void> publish(@Valid @RequestBody ProductDTO dto) {
        productService.publishProduct(SecurityUtil.getCurrentUserId(), dto);
        return Result.ok("发布成功，等待审核");
    }

    @ApiOperation("修改商品")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProductDTO dto) {
        productService.updateProduct(SecurityUtil.getCurrentUserId(), dto);
        return Result.success();
    }

    @ApiOperation("商品列表（分页）")
    @GetMapping("/list")
    public Result<IPage<Product>> list(ProductQueryDTO query) {
        if (query.getStatus() == null) {
            query.setStatus(1);
        }
        return Result.success(productService.getProductPage(query));
    }

    @ApiOperation("我发布的商品")
    @GetMapping("/my")
    public Result<IPage<Product>> myProducts(ProductQueryDTO query) {
        query.setUserId(SecurityUtil.getCurrentUserId());
        return Result.success(productService.getProductPage(query));
    }

    @ApiOperation("商品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id, SecurityUtil.getCurrentUserId()));
    }

    @ApiOperation("下架商品")
    @PutMapping("/off/{id}")
    public Result<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("收藏/取消收藏")
    @PostMapping("/favorite/{id}")
    public Result<Void> toggleFavorite(@PathVariable Long id) {
        productService.toggleFavorite(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("个性推荐")
    @GetMapping("/recommend")
    public Result<IPage<Product>> recommend(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "8") int pageSize) {
        Long userId = null;
        try { userId = SecurityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        return Result.success(productService.getRecommendProducts(userId, pageNum, pageSize));
    }

    @ApiOperation("我的收藏")
    @GetMapping("/favorites")
    public Result<IPage<Product>> myFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(productService.getMyFavorites(SecurityUtil.getCurrentUserId(), pageNum, pageSize));
    }
}
