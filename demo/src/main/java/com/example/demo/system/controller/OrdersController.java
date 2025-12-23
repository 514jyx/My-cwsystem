package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.system.dto.OrderWithItemsDTO;
import com.example.demo.system.dto.SalesOrderWithItemsDTO;
import com.example.demo.system.entity.Orders;
import com.example.demo.system.entity.OrderItems;
import com.example.demo.system.service.IOrdersService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/orders")
@CrossOrigin
public class OrdersController {

    @Resource
    private IOrdersService ordersService;

    // 原有采购订单接口（不变）
    @GetMapping("/purchase/page")
    public Result queryPurchaseOrderPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            IPage<Orders> page = ordersService.queryPurchaseOrderPage(
                    pageNum, pageSize, orderNo, supplierId, status, startDate, endDate);
            Map<String, Object> data = new HashMap<>();
            data.put("list", page.getRecords());
            data.put("total", page.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    // 原有采购订单其他接口（add/update/detail，不变）
    @PostMapping("/purchase/add")
    public Result addPurchaseOrder(@Valid @RequestBody OrderWithItemsDTO dto) {
        try {
            return ordersService.addPurchaseOrder(dto) ?
                    Result.success("新增成功") : Result.fail("新增失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/purchase/update/{id}")
    public Result updatePurchaseOrder(
            @PathVariable Long id, @Valid @RequestBody OrderWithItemsDTO dto) {
        try {
            dto.setId(id);
            return ordersService.updatePurchaseOrder(dto) ?
                    Result.success("编辑成功") : Result.fail("编辑失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/purchase/detail/{id}")
    public Result getPurchaseOrderDetail(@PathVariable Long id) {
        try {
            Orders order = ordersService.getPurchaseOrderDetail(id);
            if (order == null) return Result.fail("订单不存在");
            List<OrderItems> items = ordersService.getOrderItemsByOrderId(id);
            Map<String, Object> data = new HashMap<>();
            data.put("order", order);
            data.put("items", items);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("查询详情失败：" + e.getMessage());
        }
    }

    // -------------------------- 修复：采购订单状态变更接口（补全return逻辑） --------------------------
    @PutMapping("/purchase/confirm/{id}")
    public Result confirmOrder(@PathVariable Long id) {
        try {
            // 调用采购单确认的Service方法（与销售单区分开）
            return ordersService.confirmPurchaseOrder(id) ?
                    Result.success("采购订单确认成功") : Result.fail("采购订单确认失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/purchase/complete/{id}")
    public Result completeOrder(@PathVariable Long id) {
        try {
            // 调用采购单完成的Service方法
            return ordersService.completePurchaseOrder(id) ?
                    Result.success("采购订单完成成功") : Result.fail("采购订单完成失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/purchase/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        try {
            // 调用采购单取消的Service方法
            return ordersService.cancelPurchaseOrder(id) ?
                    Result.success("采购订单取消成功") : Result.fail("采购订单取消失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/purchase/delete/{id}")
    public Result deletePurchaseOrder(@PathVariable Long id) {
        try {
            return ordersService.deletePurchaseOrder(id) ?
                    Result.success("删除成功") : Result.fail("删除失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    // -------------------------- 销售订单核心接口（不变） --------------------------
    @GetMapping("/sales/page")
    public Result querySalesOrderPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            IPage<Orders> page = ordersService.querySalesOrderPage(
                    pageNum, pageSize, orderNo, customerId, status, startDate, endDate);
            Map<String, Object> data = new HashMap<>();
            data.put("list", page.getRecords());
            data.put("total", page.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("销售订单查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/sales/detail/{id}")
    public Result getSalesOrderDetail(@PathVariable Long id) {
        try {
            Orders order = ordersService.getSalesOrderDetail(id);
            if (order == null) return Result.fail("销售订单不存在");
            List<OrderItems> items = ordersService.getOrderItemsByOrderId(id);
            Map<String, Object> data = new HashMap<>();
            data.put("order", order);
            data.put("items", items);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("销售订单详情查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/sales/add")
    public Result addSalesOrder(@Valid @RequestBody SalesOrderWithItemsDTO dto) {
        try {
            return ordersService.addSalesOrder(dto) ?
                    Result.success("新增销售订单成功") : Result.fail("新增销售订单失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/sales/update/{id}")
    public Result updateSalesOrder(
            @PathVariable Long id, @Valid @RequestBody SalesOrderWithItemsDTO dto) {
        try {
            dto.setId(id);
            return ordersService.updateSalesOrder(dto) ?
                    Result.success("编辑销售订单成功") : Result.fail("编辑销售订单失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @DeleteMapping("/sales/delete/{id}")
    public Result deleteSalesOrder(@PathVariable Long id) {
        try {
            return ordersService.deleteSalesOrder(id) ?
                    Result.success("删除销售订单成功") : Result.fail("删除销售订单失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/sales/confirm/{id}")
    public Result confirmSalesOrder(@PathVariable Long id) {
        try {
            return ordersService.confirmSalesOrder(id) ?
                    Result.success("销售订单确认成功") : Result.fail("销售订单确认失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/sales/complete/{id}")
    public Result completeSalesOrder(@PathVariable Long id) {
        try {
            return ordersService.completeSalesOrder(id) ?
                    Result.success("销售订单完成成功") : Result.fail("销售订单完成失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/sales/cancel/{id}")
    public Result cancelSalesOrder(@PathVariable Long id) {
        try {
            return ordersService.cancelSalesOrder(id) ?
                    Result.success("销售订单取消成功") : Result.fail("销售订单取消失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}