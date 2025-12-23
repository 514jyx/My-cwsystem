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
@TableName("customers")
@Accessors(chain = true)
@ApiModel(value = "Customers对象", description = "")
public class Customers implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户名称
     */
    @TableField("name")
    @ApiModelProperty("客户名称")
    private String name;

    /**
     * 电话
     */
    @TableField("phone")
    @ApiModelProperty("电话")
    private String phone;

    /**
     * 地址
     */
    @TableField("address")
    @ApiModelProperty("地址")
    private String address;

    /**
     * 应收余额
     */
    @TableField("balance")
    @ApiModelProperty("应收余额")
    private BigDecimal balance;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
