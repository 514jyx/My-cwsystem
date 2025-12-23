package com.example.demo.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.system.dto.OrderWithItemsDTO;
import com.example.demo.system.dto.SalesOrderWithItemsDTO;
import com.example.demo.system.entity.Orders;
import com.example.demo.system.entity.OrderItems;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IOrdersService extends IService<Orders> {
    // 1. 分页查询采购订单（无变更）
    IPage<Orders> queryPurchaseOrderPage(
            Integer pageNum, Integer pageSize,
            String orderNo, Long supplierId, String status,
            String startDate, String endDate
    );

    // 2. 新增采购订单（含订单项）
    boolean addPurchaseOrder(OrderWithItemsDTO dto);

    // 3. 编辑采购订单（含订单项）
    boolean updatePurchaseOrder(OrderWithItemsDTO dto);

    // 4. 查看采购订单详情
    Orders getPurchaseOrderDetail(Long id);

    // 5. 查看订单关联的订单项
    List<OrderItems> getOrderItemsByOrderId(Long orderId);

    // -------------------------- 核心修正：采购单状态方法名与Controller一致 --------------------------
    boolean confirmPurchaseOrder(Long id); // 原 confirmOrder → 同步Controller方法名
    boolean completePurchaseOrder(Long id); // 原 completeOrder → 同步Controller方法名
    boolean cancelPurchaseOrder(Long id); // 原 cancelOrder → 同步Controller方法名

    // 其他采购单接口（无变更）
    boolean deletePurchaseOrder(Long id);
    void updateStatus(Long id, String status);

    // -------------------------- 销售单接口（无变更，已匹配） --------------------------
    IPage<Orders> querySalesOrderPage(
            Integer pageNum, Integer pageSize,
            String orderNo, Long customerId, String status,
            String startDate, String endDate
    );
    Orders getSalesOrderDetail(Long id);
    boolean addSalesOrder(SalesOrderWithItemsDTO dto);
    boolean updateSalesOrder(SalesOrderWithItemsDTO dto);
    boolean deleteSalesOrder(Long id);
    boolean confirmSalesOrder(Long id);
    boolean completeSalesOrder(Long id);
    boolean cancelSalesOrder(Long id);
}