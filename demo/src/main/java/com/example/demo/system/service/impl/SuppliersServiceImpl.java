package com.example.demo.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.Suppliers;
import com.example.demo.system.mapper.SuppliersMapper;
import com.example.demo.system.service.ISuppliersService;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Service
public class SuppliersServiceImpl extends ServiceImpl<SuppliersMapper, Suppliers> implements ISuppliersService {

    @Override
    public IPage<Suppliers> getPage(Page<Suppliers> page, String name) {
        LambdaQueryWrapper<Suppliers> queryWrapper = new LambdaQueryWrapper<>();
        // 模糊查询供应商名称
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Suppliers::getName, name);
        }
        // 按创建时间降序
        queryWrapper.orderByAsc(Suppliers::getCreatedAt);
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Suppliers getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean saveSuppliers(Suppliers suppliers) {
        // 设置创建时间和更新时间
        Date now = new Date();
        suppliers.setCreatedAt(now);
        suppliers.setUpdatedAt(now);
        return save(suppliers);
    }

    @Override
    public boolean updateSuppliers(Suppliers suppliers) {
        // 更新时只更新修改时间
        suppliers.setUpdatedAt(new Date());
        return updateById(suppliers);
    }

    @Override
    public boolean removeSuppliers(Long id) {
        return removeById(id);
    }
}
