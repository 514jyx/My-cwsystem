package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.dto.OrderItemsDTO;
import com.example.demo.system.dto.OrderWithItemsDTO;
import com.example.demo.system.dto.SalesOrderWithItemsDTO;
import com.example.demo.system.entity.Orders;
import com.example.demo.system.entity.OrderItems;
import com.example.demo.system.entity.Suppliers;
import com.example.demo.system.entity.Customers;
import com.example.demo.system.mapper.OrdersMapper;
import com.example.demo.system.mapper.OrderItemsMapper;
import com.example.demo.system.mapper.SuppliersMapper;
import com.example.demo.system.mapper.CustomersMapper;
import com.example.demo.system.service.IOrdersService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    private static final Logger log = Logger.getLogger(OrdersServiceImpl.class.getName());
    @Resource private OrdersMapper ordersMapper;
    @Resource private OrderItemsMapper orderItemsMapper;
    @Resource private SuppliersMapper suppliersMapper;
    @Resource private CustomersMapper customersMapper;

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat ORDER_NO_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    private static final Random RANDOM = new Random();

    // ==================== 采购订单原有方法（修复方法名同步）====================
    @Override
    public IPage<Orders> queryPurchaseOrderPage(
            Integer pageNum, Integer pageSize, String orderNo, Long supplierId, String status,
            String startDate, String endDate) {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Orders> page = new Page<>(pageNum, pageSize);
        return ordersMapper.selectPurchaseOrderPage(page, orderNo, supplierId, status, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPurchaseOrder(OrderWithItemsDTO dto) {
        try {
            Suppliers supplier = suppliersMapper.selectById(dto.getSupplierId());
            if (supplier == null) throw new RuntimeException("供应商不存在");

            String orderNo = generatePurchaseOrderNo();

            Orders order = new Orders()
                    .setOrderNo(orderNo)
                    .setOrderType("采购")
                    .setSupplierId(dto.getSupplierId())
                    .setCustomerId(null)
                    .setStatus("草稿")
                    .setPaidAmount(dto.getPaidAmount())
                    .setTransactionId(dto.getTransactionId());

            order.setOrderDate(DATE_FORMATTER.parse(dto.getOrderDate()));

            List<OrderItems> newOrderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemsDTO itemDto : dto.getItemList()) {
                BigDecimal quantityBig = BigDecimal.valueOf(itemDto.getQuantity());
                BigDecimal itemAmount = quantityBig.multiply(itemDto.getUnitPrice());
                OrderItems orderItem = new OrderItems()
                        .setProductId(itemDto.getProductId())
                        .setProductName(itemDto.getProductName())
                        .setQuantity(itemDto.getQuantity())
                        .setUnitPrice(itemDto.getUnitPrice())
                        .setAmount(itemAmount);
                newOrderItems.add(orderItem);
                totalAmount = totalAmount.add(itemAmount);
            }
            order.setTotalAmount(totalAmount);

            boolean saveOrder = this.save(order);
            if (!saveOrder) throw new RuntimeException("订单主表保存失败");

            newOrderItems.forEach(item -> item.setOrderId(order.getId()));
            boolean saveItems = newOrderItems.stream().allMatch(item -> orderItemsMapper.insert(item) > 0);
            if (!saveItems) throw new RuntimeException("订单项保存失败");

            return true;
        } catch (ParseException e) {
            throw new RuntimeException("订单日期格式错误（yyyy-MM-dd）");
        } catch (RuntimeException e) {
            log.severe("新增订单失败：" + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePurchaseOrder(OrderWithItemsDTO dto) {
        try {
            Orders order = this.getById(dto.getId());
            if (order == null) throw new RuntimeException("订单不存在");
            if (!"草稿".equals(order.getStatus())) throw new RuntimeException("仅草稿可编辑");

            if (suppliersMapper.selectById(dto.getSupplierId()) == null) throw new RuntimeException("供应商不存在");

            order.setSupplierId(dto.getSupplierId())
                    .setPaidAmount(dto.getPaidAmount())
                    .setTransactionId(dto.getTransactionId())
                    .setOrderDate(DATE_FORMATTER.parse(dto.getOrderDate()));

            List<OrderItems> newOrderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemsDTO itemDto : dto.getItemList()) {
                BigDecimal quantityBig = BigDecimal.valueOf(itemDto.getQuantity());
                BigDecimal itemAmount = quantityBig.multiply(itemDto.getUnitPrice());
                newOrderItems.add(new OrderItems()
                        .setOrderId(order.getId())
                        .setProductId(itemDto.getProductId())
                        .setProductName(itemDto.getProductName())
                        .setQuantity(itemDto.getQuantity())
                        .setUnitPrice(itemDto.getUnitPrice())
                        .setAmount(itemAmount));
                totalAmount = totalAmount.add(itemAmount);
            }
            order.setTotalAmount(totalAmount);

            boolean updateOrder = this.updateById(order);
            if (!updateOrder) throw new RuntimeException("订单主表更新失败");
            orderItemsMapper.deleteByOrderId(order.getId());
            boolean saveItems = newOrderItems.stream().allMatch(item -> orderItemsMapper.insert(item) > 0);
            if (!saveItems) throw new RuntimeException("订单项更新失败");

            return true;
        } catch (ParseException e) {
            throw new RuntimeException("订单日期格式错误");
        } catch (RuntimeException e) {
            log.severe("编辑订单失败：" + e.getMessage());
            throw e;
        }
    }

    @Override
    public Orders getPurchaseOrderDetail(Long id) {
        return ordersMapper.selectPurchaseOrderById(id);
    }

    @Override
    public List<OrderItems> getOrderItemsByOrderId(Long orderId) {
        return orderItemsMapper.selectByOrderId(orderId);
    }

    // -------------------------- 核心修复1：采购单状态方法名同步接口 --------------------------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPurchaseOrder(Long id) { // 方法名与接口一致
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("采购订单不存在");
        if (!"采购".equals(order.getOrderType())) throw new RuntimeException("仅采购订单可确认");
        if (!"草稿".equals(order.getStatus())) throw new RuntimeException("仅草稿可确认");
        order.setStatus("已确认");
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completePurchaseOrder(Long id) { // 方法名与接口一致
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("采购订单不存在");
        if (!"采购".equals(order.getOrderType())) throw new RuntimeException("仅采购订单可完成");
        if (!"已确认".equals(order.getStatus())) throw new RuntimeException("仅已确认可完成");
        order.setStatus("已完成");
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPurchaseOrder(Long id) { // 方法名与接口一致
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("采购订单不存在");
        if (!"采购".equals(order.getOrderType())) throw new RuntimeException("仅采购订单可取消");
        if ("已完成".equals(order.getStatus())) throw new RuntimeException("已完成订单不可取消");
        order.setStatus("已取消");
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePurchaseOrder(Long id) {
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!"采购".equals(order.getOrderType())) throw new RuntimeException("仅采购订单可删除");
        if (!"草稿".equals(order.getStatus())) throw new RuntimeException("仅草稿可删除");
        orderItemsMapper.deleteByOrderId(id);
        return this.removeById(id);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Orders order = baseMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(status);
        baseMapper.updateById(order);
    }

    private String generatePurchaseOrderNo() {
        String dateStr = ORDER_NO_FORMATTER.format(new Date());
        String randomStr = String.format("%03d", RANDOM.nextInt(999) + 1);
        String orderNo = "CG" + dateStr + randomStr;
        if (this.lambdaQuery().eq(Orders::getOrderNo, orderNo).one() != null) {
            return generatePurchaseOrderNo();
        }
        return orderNo;
    }

    // ==================== 新增：销售订单方法（补全状态变更实现）====================
    @Override
    public IPage<Orders> querySalesOrderPage(
            Integer pageNum, Integer pageSize, String orderNo, Long customerId, String status,
            String startDate, String endDate) {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        Page<Orders> page = new Page<>(pageNum, pageSize);
        return ordersMapper.selectSalesOrderPage(page, orderNo, customerId, status, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSalesOrder(SalesOrderWithItemsDTO dto) {
        try {
            Customers customer = customersMapper.selectById(dto.getCustomerId());
            if (customer == null) throw new RuntimeException("客户不存在");

            String orderNo = generateSalesOrderNo();

            Orders order = new Orders()
                    .setOrderNo(orderNo)
                    .setOrderType("销售")
                    .setCustomerId(dto.getCustomerId())
                    .setSupplierId(null)
                    .setStatus("草稿")
                    .setPaidAmount(dto.getPaidAmount())
                    .setTransactionId(dto.getTransactionId());

            order.setOrderDate(DATE_FORMATTER.parse(dto.getOrderDate()));

            List<OrderItems> newOrderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemsDTO itemDto : dto.getItemList()) {
                BigDecimal quantityBig = BigDecimal.valueOf(itemDto.getQuantity());
                BigDecimal itemAmount = quantityBig.multiply(itemDto.getUnitPrice());
                OrderItems orderItem = new OrderItems()
                        .setProductId(itemDto.getProductId())
                        .setProductName(itemDto.getProductName())
                        .setQuantity(itemDto.getQuantity())
                        .setUnitPrice(itemDto.getUnitPrice())
                        .setAmount(itemAmount);
                newOrderItems.add(orderItem);
                totalAmount = totalAmount.add(itemAmount);
            }
            order.setTotalAmount(totalAmount);

            boolean saveOrder = this.save(order);
            if (!saveOrder) throw new RuntimeException("销售订单主表保存失败");

            newOrderItems.forEach(item -> item.setOrderId(order.getId()));
            boolean saveItems = newOrderItems.stream().allMatch(item -> orderItemsMapper.insert(item) > 0);
            if (!saveItems) throw new RuntimeException("销售订单项保存失败");

            return true;
        } catch (ParseException e) {
            throw new RuntimeException("订单日期格式错误（yyyy-MM-dd）");
        } catch (RuntimeException e) {
            log.severe("新增销售订单失败：" + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSalesOrder(SalesOrderWithItemsDTO dto) {
        try {
            Orders order = this.getById(dto.getId());
            if (order == null) throw new RuntimeException("销售订单不存在");
            if (!"销售".equals(order.getOrderType())) throw new RuntimeException("仅销售订单可编辑");
            if (!"草稿".equals(order.getStatus())) throw new RuntimeException("仅草稿状态可编辑");

            Customers customer = customersMapper.selectById(dto.getCustomerId());
            if (customer == null) throw new RuntimeException("客户不存在");

            order.setCustomerId(dto.getCustomerId())
                    .setPaidAmount(dto.getPaidAmount())
                    .setTransactionId(dto.getTransactionId())
                    .setOrderDate(DATE_FORMATTER.parse(dto.getOrderDate()));

            List<OrderItems> newOrderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemsDTO itemDto : dto.getItemList()) {
                BigDecimal quantityBig = BigDecimal.valueOf(itemDto.getQuantity());
                BigDecimal itemAmount = quantityBig.multiply(itemDto.getUnitPrice());
                newOrderItems.add(new OrderItems()
                        .setOrderId(order.getId())
                        .setProductId(itemDto.getProductId())
                        .setProductName(itemDto.getProductName())
                        .setQuantity(itemDto.getQuantity())
                        .setUnitPrice(itemDto.getUnitPrice())
                        .setAmount(itemAmount));
                totalAmount = totalAmount.add(itemAmount);
            }
            order.setTotalAmount(totalAmount);

            boolean updateOrder = this.updateById(order);
            if (!updateOrder) throw new RuntimeException("销售订单主表更新失败");

            orderItemsMapper.deleteByOrderId(order.getId());
            boolean saveItems = newOrderItems.stream().allMatch(item -> orderItemsMapper.insert(item) > 0);
            if (!saveItems) throw new RuntimeException("销售订单项更新失败");

            return true;
        } catch (ParseException e) {
            throw new RuntimeException("订单日期格式错误");
        } catch (RuntimeException e) {
            log.severe("编辑销售订单失败：" + e.getMessage());
            throw e;
        }
    }

    @Override
    public Orders getSalesOrderDetail(Long id) {
        return ordersMapper.selectSalesOrderById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSalesOrder(Long id) {
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("销售订单不存在");
        if (!"销售".equals(order.getOrderType())) throw new RuntimeException("仅销售订单可删除");
        if (!"草稿".equals(order.getStatus())) throw new RuntimeException("仅草稿状态可删除");
        orderItemsMapper.deleteByOrderId(id);
        return this.removeById(id);
    }

    // -------------------------- 核心修复2：补全销售单状态变更方法实现 --------------------------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmSalesOrder(Long id) {
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("销售订单不存在");
        if (!"销售".equals(order.getOrderType())) throw new RuntimeException("仅销售订单可确认");
        if (!"草稿".equals(order.getStatus())) throw new RuntimeException("仅草稿可确认");
        order.setStatus("已确认");
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeSalesOrder(Long id) {
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("销售订单不存在");
        if (!"销售".equals(order.getOrderType())) throw new RuntimeException("仅销售订单可完成");
        if (!"已确认".equals(order.getStatus())) throw new RuntimeException("仅已确认可完成");
        order.setStatus("已完成");
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSalesOrder(Long id) {
        Orders order = this.getById(id);
        if (order == null) throw new RuntimeException("销售订单不存在");
        if (!"销售".equals(order.getOrderType())) throw new RuntimeException("仅销售订单可取消");
        if ("已完成".equals(order.getStatus())) throw new RuntimeException("已完成订单不可取消");
        order.setStatus("已取消");
        return this.updateById(order);
    }

    // 生成销售订单编号（前缀 XS = 销售）
    private String generateSalesOrderNo() {
        String dateStr = ORDER_NO_FORMATTER.format(new Date());
        String randomStr = String.format("%03d", RANDOM.nextInt(999) + 1);
        String orderNo = "XS" + dateStr + randomStr;
        if (this.lambdaQuery().eq(Orders::getOrderNo, orderNo).one() != null) {
            return generateSalesOrderNo();
        }
        return orderNo;
    }
}