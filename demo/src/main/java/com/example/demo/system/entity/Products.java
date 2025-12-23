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
import java.time.LocalDate; // 对应数据库 DATE 类型

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
@TableName("products")
@Accessors(chain = true)
@ApiModel(value = "Products对象", description = "商品信息")
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "商品ID", hidden = true)
    private Long id;

    /**
     * 商品名称
     */
    @TableField("name")
    @ApiModelProperty(value = "商品名称", required = true, example = "华为Mate60")
    private String name;

    /**
     * 分类
     */
    @ApiModelProperty(value = "商品分类", example = "手机")
    @TableField("category")
    private String category;

    /**
     * 单位
     */
    @TableField("unit")
    @ApiModelProperty(value = "计量单位", example = "台", notes = "默认值：个")
    private String unit;

    /**
     * 成本价
     */
    @ApiModelProperty(value = "成本价", example = "3999.00")
    @TableField("cost_price")
    private BigDecimal costPrice;

    /**
     * 销售价
     */
    @ApiModelProperty(value = "销售价", required = true, example = "5999.00")
    @TableField("sale_price")
    private BigDecimal salePrice;

    /**
     * 库存
     */
    @TableField("stock")
    @ApiModelProperty(value = "库存数量", example = "100", notes = "默认值：0")
    private Integer stock;

    // 核心修改：LocalDate 对应数据库 DATE 类型
    @TableField(value = "created_at", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @ApiModelProperty(value = "创建日期", hidden = true)
    private LocalDate createdAt;

    // 核心修改：LocalDate 对应数据库 DATE 类型
    @TableField(value = "updated_at", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新日期", hidden = true)
    private LocalDate updatedAt;
}