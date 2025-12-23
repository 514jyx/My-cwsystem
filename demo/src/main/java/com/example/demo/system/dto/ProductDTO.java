package com.example.demo.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 商品DTO（接收前端新增/编辑表单参数）
 */
@Data
@ApiModel(value = "ProductDTO", description = "商品新增/编辑表单参数")
public class ProductDTO {

    @ApiModelProperty(value = "商品ID（编辑时必填，新增时无需传）", hidden = true)
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Size(min = 1, max = 100, message = "商品名称长度不能超过100个字符")
    @ApiModelProperty(value = "商品名称", required = true, example = "华为Mate60")
    private String name;

    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @ApiModelProperty(value = "商品分类", example = "手机")
    private String category;

    @Size(max = 10, message = "计量单位长度不能超过10个字符")
    @ApiModelProperty(value = "计量单位", example = "台", notes = "默认值：个")
    private String unit;

    @DecimalMin(value = "0.00", message = "成本价不能为负数") // 新增校验
    @ApiModelProperty(value = "成本价", example = "3999.00")
    private BigDecimal costPrice;

    @NotNull(message = "销售价不能为空")
    @DecimalMin(value = "0.01", message = "销售价必须大于0") // 替换 @Range 为 @DecimalMin
    @ApiModelProperty(value = "销售价", required = true, example = "5999.00")
    private BigDecimal salePrice;


    @Range(min = 0, message = "库存数量不能为负数")
    @ApiModelProperty(value = "库存数量", example = "100", notes = "默认值：0")
    private Integer stock;
}
