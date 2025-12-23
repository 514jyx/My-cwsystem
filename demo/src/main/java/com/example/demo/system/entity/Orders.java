package com.example.demo.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
@TableName("orders")
@Accessors(chain = true)
@ApiModel(value = "Orders对象", description = "")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_no")
    @ApiModelProperty("订单号")
    private String orderNo;

    /**
     * 订单类型
     */
    @ApiModelProperty("订单类型")
    @TableField("order_type")
    private String orderType;

    /**
     * 供应商ID
     */
    @ApiModelProperty("供应商ID")
    @TableField("supplier_id")
    private Long supplierId;

    /**
     * 客户ID
     */
    @ApiModelProperty("客户ID")
    @TableField("customer_id")
    private Long customerId;

    /**
     * 订单日期
     */
    @ApiModelProperty("订单日期")
    @TableField("order_date")
    private Date orderDate;

    /**
     * 状态
     */
    @TableField("status")
    @ApiModelProperty("状态")
    private String status;

    /**
     * 订单总额
     */
    @ApiModelProperty("订单总额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 已付金额
     */
    @ApiModelProperty("已付金额")
    @TableField("paid_amount")
    private BigDecimal paidAmount;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    /**
     * 关联交易ID
     */
    @ApiModelProperty("关联交易ID")
    @TableField("transaction_id")
    private Long transactionId;

    @TableField(exist = false)
    private String supplierName;
}
