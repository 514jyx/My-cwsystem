package com.example.demo.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 财务交易主表（记录每笔业务的核心信息，关联分录表实现复式记账）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("transactions") // 与数据库表名一致，无需修改
@ApiModel(value = "Transactions对象", description = "财务交易主表（采购/销售/收款/付款等业务的核心记录）")
public class Transactions implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("交易ID（自增主键）")
    private Long id;

    /**
     * 交易编号
     */
    @TableField("trans_no")
    @ApiModelProperty(value = "交易编号（唯一标识，如：TR20251215001）", required = true)
    private String transNo;

    /**
     * 交易日期
     */
    @TableField("trans_date")
    @ApiModelProperty(value = "交易日期（业务发生日期）", required = true)
    private LocalDate transDate; // 核心修改：Date → LocalDate（适配日期类型，前端更易处理）

    /**
     * 交易类型（固定枚举值）
     */
    @TableField("trans_type")
    @ApiModelProperty(value = "交易类型（枚举：采购/销售/收款/付款/费用/收入/转账）", required = true,
            allowableValues = "采购,销售,收款,付款,费用,收入,转账")
    private String transType;

    /**
     * 交易说明
     */
    @TableField("description")
    @ApiModelProperty(value = "交易说明（如：2025年12月工资收入、办公室租金支付）", required = true)
    private String description;

    /**
     * 状态（固定枚举值）
     */
    @TableField("status")
    @ApiModelProperty(value = "交易状态（枚举：草稿/已过账/已取消）", required = true,
            allowableValues = "草稿,已过账,已取消")
    private String status;

    /**
     * 关联订单ID
     */
    @TableField("order_id")
    @ApiModelProperty(value = "关联订单ID（无关联订单时为null）")
    private Long orderId;

    // 👉 新增：红字冲销关联的原凭证ID字段
    /**
     * 红字冲销关联的原凭证ID（非冲销凭证则为null）
     */
    @TableField("write_off_target_id") // 对应数据库表的 write_off_target_id 字段
    @ApiModelProperty(value = "红字冲销关联的原凭证ID（仅红字冲销凭证有值，非冲销凭证为null）")
    private Long writeOffTargetId;

    /**
     * 创建时间（自动生成）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT) // 新增：插入时自动填充
    @ApiModelProperty(value = "创建时间（自动生成，格式：yyyy-MM-dd HH:mm:ss）")
    private LocalDateTime createdAt; // 核心修改：Date → LocalDateTime（JDK8 新时间类型，更规范）

    /**
     * 更新时间（自动更新）
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) // 新增：插入/更新时自动填充
    @ApiModelProperty(value = "更新时间（自动更新，格式：yyyy-MM-dd HH:mm:ss）")
    private LocalDateTime updatedAt; // 核心修改：Date → LocalDateTime
}