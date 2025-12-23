package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersMapper extends BaseMapper<Orders> {
    // 1. 分页查询采购订单（查询功能）
    IPage<Orders> selectPurchaseOrderPage(
            Page<Orders> page,
            @Param("orderNo") String orderNo,
            @Param("supplierId") Long supplierId,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
    IPage<Orders> selectSalesOrderPage(
            Page<Orders> page,
            @Param("orderNo") String orderNo,
            @Param("customerId") Long customerId, // 销售订单按客户ID筛选
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    // 2. 查询采购订单详情（详情功能，关联供应商名称）
    Orders selectPurchaseOrderById(@Param("id") Long id);
    Orders selectSalesOrderById(@Param("id") Long id);
}
