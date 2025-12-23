package com.example.demo.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 交易分录表（复式记账核心，每笔交易对应至少2条分录，确保借贷平衡）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("entries") // 与数据库表名一致（你的表名是 entries，无需修改）
@ApiModel(value = "Entries对象", description = "交易分录表（复式记账核心，记录每笔交易的借贷方向和关联科目）")
public class Entries implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("分录ID（自增主键）")
    private Long id;

    /**
     * 关联交易ID（外键）
     */
    @TableField("transaction_id")
    @ApiModelProperty(value = "关联交易ID（与transactions表id关联）", required = true)
    private Long transactionId;

    /**
     * 关联科目ID（外键）
     */
    @TableField("account_id")
    @ApiModelProperty(value = "关联科目ID（与accounts表id关联）", required = true)
    private Long accountId;

    /**
     * 分录类型（借方/贷方，复式记账核心）
     */
    @TableField("entry_type")
    @ApiModelProperty(value = "分录类型（枚举：借方/贷方）", required = true, allowableValues = "借方,贷方")
    private String entryType;

    /**
     * 分录金额（正数，借贷方向由entry_type决定）
     */
    @TableField("amount")
    @ApiModelProperty(value = "分录金额（仅正数，不允许为负）", example = "5000.00", required = true)
    private BigDecimal amount;

    /**
     * 分录说明（补充交易说明的细节）
     */
    @TableField("description")
    @ApiModelProperty(value = "分录说明（如：工资收入-招商银行、租金支出-现金）")
    private String description;

    /**
     * 创建时间（数据库自动生成，后端通过MyBatis-Plus自动填充）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT) // 新增：插入时自动填充
    @ApiModelProperty(value = "创建时间（自动生成，格式：yyyy-MM-dd HH:mm:ss）")
    private LocalDateTime createdAt; // 优化：Date → LocalDateTime（规范时间类型，避免时区问题）

    // ------------- 新增关联字段（非数据库字段，用于前端展示）-------------
    @TableField(exist = false)
    @ApiModelProperty(value = "关联科目名称（前端展示用，不存数据库）")
    private String accountName; // 如：招商银行、工资收入

    @TableField(exist = false)
    @ApiModelProperty(value = "关联科目代码（前端展示用，不存数据库）")
    private String accountCode;
}