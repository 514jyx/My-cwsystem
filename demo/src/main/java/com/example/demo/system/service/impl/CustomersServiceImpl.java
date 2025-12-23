package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.entity.Customers;
import com.example.demo.system.mapper.CustomersMapper;
import com.example.demo.system.service.ICustomersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Service
public class CustomersServiceImpl extends ServiceImpl<CustomersMapper, Customers> implements ICustomersService {

    // 实现自定义方法
    @Override
    public List<Customers> getByBalanceGreaterThan(BigDecimal minBalance) {
        QueryWrapper<Customers> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("balance", minBalance);
        return baseMapper.selectList(queryWrapper);
    }
    @Override
    public List<Customers> listAllCustomers() {
        QueryWrapper<Customers> queryWrapper = new QueryWrapper<Customers>()
                .select("id", "name") // 只查需要的字段，提高效率
                .orderByAsc("id");
        List<Customers> list = baseMapper.selectList(queryWrapper);
        return list != null ? list : new ArrayList<>();
    }
}