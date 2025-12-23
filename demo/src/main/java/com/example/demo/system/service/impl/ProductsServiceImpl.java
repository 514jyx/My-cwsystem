package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.dto.ProductDTO;
import com.example.demo.system.entity.Products;
import com.example.demo.system.mapper.ProductsMapper;
import com.example.demo.system.service.IProductsService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements IProductsService {

    /**
     * 分页+模糊查询商品（适配前端查询需求）
     */
    @Override
    public IPage<Products> selectProductPage(Integer pageNum, Integer pageSize, String name, String category) {
        // 处理默认分页参数（避免前端不传时报错）
        if (pageNum == null || pageNum < 1) {
            pageNum = 1; // 默认第一页
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10; // 默认每页10条，最大100条（防止查询压力过大）
        }

        // 创建 MyBatis-Plus 分页对象
        Page<Products> page = new Page<>(pageNum, pageSize);
        // 调用 Mapper 层的分页查询方法（已在 ProductsMapper 中定义）
        return baseMapper.selectProductPage(page, name, category);
    }

    /**
     * 新增商品（封装业务规则：默认值+数据校验）
     */
    @Override
    public boolean addProduct(ProductDTO productDTO) {
        // 1. 核心业务校验（补充 DTO 注解之外的逻辑）
        // 校验销售价不能为空（DTO 已有 @NotNull，但这里双重保障）
        Assert.notNull(productDTO.getSalePrice(), "销售价不能为空");
        // 校验销售价不能低于成本价（如果成本价已填写）
        if (productDTO.getCostPrice() != null) {
            // BigDecimal 比较用 compareTo：0=相等，1=大于，-1=小于
            Assert.isTrue(productDTO.getSalePrice().compareTo(productDTO.getCostPrice()) >= 0,
                    "销售价不能低于成本价");
        }

        // 2. 填充默认值（数据库默认值兜底，避免前端漏传）
        Products product = new Products();
        product.setName(productDTO.getName())          // 商品名称（必填，DTO 已校验）
                .setCategory(productDTO.getCategory())// 商品分类（可选）
                // 单位：前端没传或传空，默认设为“个”
                .setUnit(productDTO.getUnit() == null || productDTO.getUnit().trim().isEmpty() ? "个" : productDTO.getUnit())
                .setCostPrice(productDTO.getCostPrice())// 成本价（可选）
                .setSalePrice(productDTO.getSalePrice())// 销售价（必填）
                // 库存：前端没传，默认设为0
                .setStock(productDTO.getStock() == null ? 0 : productDTO.getStock());

        // 3. 调用 MyBatis-Plus 自带的 save 方法新增数据
        return save(product);
    }

    /**
     * 编辑商品（封装业务规则：校验商品存在+数据更新）
     */
    @Override
    public boolean updateProduct(ProductDTO productDTO) {
        // 1. 校验商品ID是否存在（编辑必须传ID）
        Long productId = productDTO.getId();
        Assert.notNull(productId, "编辑商品必须传入商品ID");
        // 查数据库确认商品存在
        Products existingProduct = getById(productId);
        Assert.notNull(existingProduct, "该商品不存在或已被删除，无法编辑");

        // 2. 数据校验（销售价不能低于成本价）
        if (productDTO.getCostPrice() != null && productDTO.getSalePrice() != null) {
            Assert.isTrue(productDTO.getSalePrice().compareTo(productDTO.getCostPrice()) >= 0,
                    "销售价不能低于成本价");
        }

        // 3. 封装更新数据（只更新前端传入的有效字段）
        Products updateProduct = new Products();
        updateProduct.setId(productId)                 // 商品ID（必须）
                .setName(productDTO.getName())          // 商品名称
                .setCategory(productDTO.getCategory())// 商品分类
                .setUnit(productDTO.getUnit() == null || productDTO.getUnit().trim().isEmpty() ? "个" : productDTO.getUnit())// 单位默认值
                .setCostPrice(productDTO.getCostPrice())// 成本价
                .setSalePrice(productDTO.getSalePrice())// 销售价
                .setStock(productDTO.getStock() == null ? 0 : productDTO.getStock());// 库存默认值

        // 4. 调用 MyBatis-Plus 自带的 updateById 方法更新数据
        return updateById(updateProduct);
    }
}