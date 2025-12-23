package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Entries;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 交易分录 Mapper 接口（扩展批量插入、关联查询方法，适配复式记账）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface EntriesMapper extends BaseMapper<Entries> {

    // 1. 批量插入分录（XML 实现，支持批量保存，提升性能）
    int batchInsertEntries(@Param("list") List<Entries> entriesList);

    // 2. 按交易ID查询所有分录（关联科目表，返回科目名称/代码，前端详情用）
    @Select("SELECT " +
            "e.id, e.transaction_id AS transactionId, e.account_id AS accountId, " +
            "e.entry_type AS entryType, e.amount, e.description, e.created_at AS createdAt, " +
            "a.name AS accountName, a.code AS accountCode " + // 映射实体类的 accountName/accountCode（非数据库字段）
            "FROM entries e " +
            "LEFT JOIN accounts a ON e.account_id = a.id " + // 关联科目表
            "WHERE e.transaction_id = #{transactionId} " +
            "ORDER BY e.id ASC")
    List<Map<String, Object>> selectEntriesByTransactionId(@Param("transactionId") Long transactionId);

    // 3. 按科目ID分页查询分录（支持日期范围筛选）
    IPage<Map<String, Object>> selectEntriesByAccountId(Page<Entries> page,
                                                        @Param("accountId") Long accountId,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate);

    // 4. 按交易ID删除所有关联分录（修正：Delete 注解替代 Select 注解，执行删除操作）
    @Delete("DELETE FROM entries WHERE transaction_id = #{transactionId}")
    int deleteEntriesByTransactionId(@Param("transactionId") Long transactionId);

    // 5. 校验交易的分录是否借贷平衡（用于过账时双重校验）
    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN entry_type = '借方' THEN amount ELSE 0 END), 0) AS debitTotal, " +
            "COALESCE(SUM(CASE WHEN entry_type = '贷方' THEN amount ELSE 0 END), 0) AS creditTotal " +
            "FROM entries " +
            "WHERE transaction_id = #{transactionId}")
    Map<String, Object> checkDebitCreditBalance(@Param("transactionId") Long transactionId);

    // ========== 保留 MyBatis-Plus 原生条件构造器支持 ==========
    IPage<Entries> selectPage(Page<Entries> page, @Param(Constants.WRAPPER) Wrapper<Entries> queryWrapper);
}