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

/**
 * <p>
 * 
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-05
 */
@Getter
@Setter
@ToString
@TableName("user")
@Accessors(chain = true)
@ApiModel(value = "User对象", description = "")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 账号
     */
    @TableField("no")
    @ApiModelProperty("账号")
    private String no;

    /**
     * 名字
     */
    @TableField("name")
    @ApiModelProperty("名字")
    private String name;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @TableField("password")
    private String password;

    /**
     * 电话
     */
    @TableField("phone")
    @ApiModelProperty("电话")
    private String phone;

    /**
     * 角色 0超级管理员，1管理员，2普通账号
     */
    @TableField("role_id")
    @ApiModelProperty("角色 0超级管理员，1管理员，2普通账号")
    private Integer roleId;

    /**
     * 是否有效，Y有效，其他无效
     */
    @TableField("isValid")
    @ApiModelProperty("是否有效，Y有效，其他无效")
    private String isValid;
}
