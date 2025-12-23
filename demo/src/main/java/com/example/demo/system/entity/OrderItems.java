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
@Accessors(chain = true)
@TableName("order_items")
@ApiModel(value = "OrderItems对象", description = "")
public class OrderItems implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    @ApiModelProperty("订单ID")
    private Long orderId;

    /**
     * 商品ID
     */
    @ApiModelProperty("商品ID")
    @TableField("product_id")
    private Long productId;

    /**
     * 商品名称
     */
    @ApiModelProperty("商品名称")
    @TableField("product_name")
    private String productName;

    /**
     * 数量
     */
    @ApiModelProperty("数量")
    @TableField("quantity")
    private Integer quantity;

    /**
     * 单价
     */
    @ApiModelProperty("单价")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 金额
     */
    @TableField("amount")
    @ApiModelProperty("金额")
    private BigDecimal amount;

    @TableField("created_at")
    private Date createdAt;
}
