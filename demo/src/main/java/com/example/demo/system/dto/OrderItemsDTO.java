package com.example.demo.system.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin; // 新增导入
import java.math.BigDecimal;

/**
 * 订单项参数DTO（复数命名，与实体类、Mapper保持一致）
 */
@Data
public class OrderItemsDTO {
    private Long id; // 编辑时必填，新增时为空

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0") // 数量是 Integer，用 @Min 没问题
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0") // 修复：BigDecimal 用 @DecimalMin
    private BigDecimal unitPrice;
}