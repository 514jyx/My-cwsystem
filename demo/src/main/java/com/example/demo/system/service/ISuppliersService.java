package com.example.demo.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Suppliers;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface ISuppliersService extends IService<Suppliers> {

    // 分页查询供应商
    IPage<Suppliers> getPage(Page<Suppliers> page, String name);

    // 根据ID查询供应商
    Suppliers getById(Long id);

    // 新增供应商
    boolean saveSuppliers(Suppliers suppliers);

    // 更新供应商
    boolean updateSuppliers(Suppliers suppliers);

    // 删除供应商
    boolean removeSuppliers(Long id);
}