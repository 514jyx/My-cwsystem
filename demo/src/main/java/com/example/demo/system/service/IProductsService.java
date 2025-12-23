package com.example.demo.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.dto.ProductDTO;
import com.example.demo.system.entity.Products;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface IProductsService extends IService<Products> {

    /**
     * 分页+模糊查询商品
     * @param pageNum 页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param name 商品名称（模糊查询，可为null）
     * @param category 商品分类（模糊查询，可为null）
     * @return 分页结果（含商品列表+分页信息）
     */
    IPage<Products> selectProductPage(Integer pageNum, Integer pageSize, String name, String category);

    /**
     * 新增商品（接收 DTO 参数，封装业务规则）
     * @param productDTO 前端表单参数
     * @return 是否新增成功
     */
    boolean addProduct(ProductDTO productDTO);

    /**
     * 编辑商品（接收 DTO 参数，校验商品是否存在）
     * @param productDTO 前端表单参数（含商品ID）
     * @return 是否编辑成功
     */
    boolean updateProduct(ProductDTO productDTO);
}