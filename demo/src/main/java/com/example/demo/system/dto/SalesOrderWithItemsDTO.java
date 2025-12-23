package com.example.demo.system.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售订单+订单项组合DTO（给客户卖货，核心是 customerId）
 */
@Data
public class SalesOrderWithItemsDTO {
    private Long id; // 编辑时必填，新增时为空

    @NotNull(message = "客户ID不能为空") // 销售订单核心：客户ID（而非供应商ID）
    private Long customerId;


    @NotBlank(message = "订单日期不能为空")
    private String orderDate; // 格式：yyyy-MM-dd

    @DecimalMin(value = "0.00", message = "已付金额不能为负数")
    private BigDecimal paidAmount = BigDecimal.ZERO; // 已付金额，默认0

    private Long transactionId; // 关联交易ID（可选）

    @Size(min = 1, message = "至少添加1条销售项") // 至少1个商品
    private List<OrderItemsDTO> itemList; // 销售项列表（复用现有订单项DTO）
}
