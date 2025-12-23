package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.system.dto.ProductDTO;
import com.example.demo.system.entity.Products;
import com.example.demo.system.service.IProductsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商品表 前端控制器（暴露商品管理接口）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/system/products")
@Api(tags = "商品管理")
public class ProductsController {

    @Resource
    private IProductsService productsService;

    /**
     * 分页+模糊查询商品（核心接口，适配 Result 分页格式）
     */
    @GetMapping("/page")
    @ApiOperation("分页查询商品（支持名称、分类模糊查询）")
    public Result selectProductPage(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        IPage<Products> pageResult = productsService.selectProductPage(pageNum, pageSize, name, category);
        // 关键：Result.success(数据列表, 总条数)，匹配你的 Result 类分页格式
        return Result.success(pageResult.getRecords(), pageResult.getTotal());
    }

    /**
     * 根据ID查询商品详情
     */
    @GetMapping("/get/{id}")
    @ApiOperation("根据ID查询商品详情")
    public Result getProductById(@PathVariable Long id) {
        Products product = productsService.getById(id);
        if (product != null) {
            return Result.success(product); // 单个商品数据，直接存 data 字段
        } else {
            return Result.fail("该商品不存在或已被删除");
        }
    }

    /**
     * 新增商品
     */
    @PostMapping("/add")
    @ApiOperation("新增商品")
    public Result addProduct(@Validated @RequestBody ProductDTO productDTO) {
        boolean isSuccess = productsService.addProduct(productDTO);
        return isSuccess ? Result.success("商品新增成功") : Result.fail("商品新增失败");
    }

    /**
     * 编辑商品
     */
    @PutMapping("/update")
    @ApiOperation("编辑商品")
    public Result updateProduct(@Validated @RequestBody ProductDTO productDTO) {
        boolean isSuccess = productsService.updateProduct(productDTO);
        return isSuccess ? Result.success("商品编辑成功") : Result.fail("商品编辑失败");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除商品")
    public Result deleteProduct(@PathVariable Long id) {
        boolean isSuccess = productsService.removeById(id);
        return isSuccess ? Result.success("商品删除成功") : Result.fail("商品删除失败");
    }
}