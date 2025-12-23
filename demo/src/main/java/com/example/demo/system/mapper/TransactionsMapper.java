package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Transactions;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 财务交易 Mapper 接口（扩展最近交易、分页搜索、编号生成方法）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface TransactionsMapper extends BaseMapper<Transactions> {

    // 1. 查询最近交易记录（前端财务首页用，默认查10条已过账的交易）
    // 按交易日期倒序，取最新的N条
    // 修正：移除 amount 字段（Transactions 实体类已删除该字段，避免查询字段不匹配）
    @Select("SELECT id, trans_no, trans_date, trans_type, description, status, order_id, created_at, updated_at " +
            "FROM transactions " +
            "WHERE status = '已过账' " +
            "ORDER BY trans_date DESC " +
            "LIMIT #{limit}")
    List<Transactions> selectRecentTransactions(@Param("limit") Integer limit);

    // 2. 分页搜索交易（支持按交易编号、类型、状态、日期范围筛选）
    // 复杂条件查询，推荐在 XML 中实现，这里仅定义接口
    IPage<Transactions> selectTransactionPage(Page<Transactions> page,
                                              @Param("transNo") String transNo,
                                              @Param("transType") String transType,
                                              @Param("status") String status,
                                              @Param("startDate") String startDate, // 日期范围起始（格式：yyyy-MM-dd）
                                              @Param("endDate") String endDate);   // 日期范围结束（格式：yyyy-MM-dd）

    // 3. 生成唯一交易编号（规则：TR + 年月日 + 3位流水号，如 TR20251215001）
    // 作用：新增交易时自动生成，避免手动输入重复
    // 修正：用 COALESCE 替代 IFNULL（兼容更多数据库，如 PostgreSQL），避免空指针
    @Select("SELECT CONCAT('TR', DATE_FORMAT(NOW(), '%Y%m%d'), " +
            "LPAD(COALESCE(MAX(SUBSTRING(trans_no, 11)), 0) + 1, 3, '0')) " +
            "FROM transactions " +
            "WHERE trans_no LIKE CONCAT('TR', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String generateTransNo();

    // 4. 根据交易ID查询关联的所有分录（删除交易时校验，或详情展示用）
    @Select("SELECT COUNT(1) FROM entries WHERE transaction_id = #{transactionId}")
    int countEntriesByTransactionId(@Param("transactionId") Long transactionId);

    // ========== 保留 MyBatis-Plus 原生条件构造器支持 ==========
    IPage<Transactions> selectPage(Page<Transactions> page, @Param(Constants.WRAPPER) Wrapper<Transactions> queryWrapper);
}