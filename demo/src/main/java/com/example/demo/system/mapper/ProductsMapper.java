package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Products;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface ProductsMapper extends BaseMapper<Products> {

    /**
     * 分页+模糊查询商品（按名称、分类）
     * @param page 分页参数（pageNum 页码，pageSize 每页条数）
     * @param name 商品名称（模糊查询，可为null）
     * @param category 商品分类（模糊查询，可为null）
     * @return 分页结果（包含商品列表和总条数）
     */
    IPage<Products> selectProductPage(Page<Products> page,
                                      @Param("name") String name,
                                      @Param("category") String category);

    /**
     * （可选）带条件构造器的分页查询（灵活扩展，比如多条件组合查询）
     * @param page 分页参数
     * @param queryWrapper 条件构造器（MyBatis-Plus 自带，支持复杂条件）
     * @return 分页结果
     */
    IPage<Products> selectProductPage(IPage<Products> page,
                                      @Param(Constants.WRAPPER) Wrapper<Products> queryWrapper);
}