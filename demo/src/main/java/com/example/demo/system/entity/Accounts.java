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
import java.util.List;

/**
 * <p>
 * 财务科目表（对应会计科目，支持层级结构）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
@TableName("accounts") // 确保与数据库表名一致（若表名改动，修改此处）
@Accessors(chain = true)
@ApiModel(value = "Accounts对象", description = "财务科目表（资产/负债/权益/收入/费用类科目，支持层级结构）")
public class Accounts implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("科目ID（自增主键）")
    private Long id;

    /**
     * 科目代码
     */
    @TableField("code")
    @ApiModelProperty(value = "科目代码（如：1001-现金、2001-短期借款）", required = true)
    private String code;

    /**
     * 科目名称
     */
    @TableField("name")
    @ApiModelProperty(value = "科目名称（如：现金、银行存款、工资收入）", required = true)
    private String name;

    /**
     * 科目类型（固定枚举值）
     */
    @TableField("type")
    @ApiModelProperty(value = "科目类型（枚举：资产/负债/权益/收入/费用）", required = true, allowableValues = "资产,负债,权益,收入,费用")
    private String type;

    /**
     * 父级科目ID
     */
    @TableField("parent_id")
    @ApiModelProperty(value = "父级科目ID（顶级科目为null，如：招商银行的父级是银行存款）")
    private Long parentId;

    /**
     * 当前余额（资产/权益/收入为正，负债/费用为负）
     */
    @TableField("balance")
    @ApiModelProperty(value = "当前余额", example = "0.00")
    private BigDecimal balance;

    @TableField("created_at")
    @ApiModelProperty(value = "创建时间（自动生成）")
    private Date createdAt;

    @TableField("updated_at")
    @ApiModelProperty(value = "更新时间（自动更新）")
    private Date updatedAt;

    // ------------- 新增字段（用于树形结构展示，非数据库字段）-------------
    @TableField(exist = false) // 标识该字段不是数据库表字段
    @ApiModelProperty(value = "子科目列表（树形结构用）")
    private List<Accounts> children;
}