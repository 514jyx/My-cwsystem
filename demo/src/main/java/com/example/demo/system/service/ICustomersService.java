package com.example.demo.system.service;

import com.example.demo.system.entity.Customers;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface ICustomersService extends IService<Customers> {
    // 可扩展自定义方法
    List<Customers> getByBalanceGreaterThan(BigDecimal minBalance);
    List<Customers> listAllCustomers();

}
