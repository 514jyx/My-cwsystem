package com.example.demo.system.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin; // 新增导入（可选，增强paidAmount校验）
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单+订单项组合DTO
 */
@Data
public class OrderWithItemsDTO {
    private Long id; // 编辑时必填，新增时为空

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "订单日期不能为空")
    private String orderDate;

    // 可选补充：校验已付金额不能为负数（避免传负数）
    @DecimalMin(value = "0.00", message = "已付金额不能为负数")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    private Long transactionId;

    // 此处正确：List<OrderItemsDTO> 与订单项DTO类名一致（复数），无错误
    @Size(min = 1, message = "至少添加1条订单项")
    private List<OrderItemsDTO> itemList;
}