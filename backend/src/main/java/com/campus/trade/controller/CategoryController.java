package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.entity.ProductCategory;
import com.campus.trade.entity.ProductTag;
import com.campus.trade.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "分类与标签接口")
@RestController
public class CategoryController {

    @Resource
    private ProductService productService;

    @ApiOperation("获取分类树")
    @GetMapping("/category/tree")
    public Result<List<ProductCategory>> getCategoryTree() {
        return Result.success(productService.getCategoryTree());
    }

    @ApiOperation("获取所有标签")
    @GetMapping("/category/tags")
    public Result<List<ProductTag>> getAllTags() {
        return Result.success(productService.getAllTags());
    }
}
