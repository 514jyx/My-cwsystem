package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Accounts;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 财务科目 Mapper 接口（扩展树形结构、财务统计、分页搜索方法）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface AccountsMapper extends BaseMapper<Accounts> {
    /**
     * 根据科目ID更新余额（交易分录保存后，同步更新科目余额）
     */
    @Update("UPDATE accounts SET balance = #{newBalance}, updated_at = NOW() WHERE id = #{accountId}")
    int updateBalanceById(@Param("accountId") Long accountId, @Param("newBalance") BigDecimal newBalance);

    IPage<Accounts> selectAccountPage(Page<Accounts> page,
                                      @Param("code") String code,
                                      @Param("name") String name,
                                      @Param("type") String type);

    @Select("SELECT id, code, name, type, parent_id, balance FROM accounts ORDER BY code ASC")
    List<Accounts> selectAllAccounts();


    @Select("SELECT type, SUM(balance) AS total_balance " +
            "FROM accounts " +
            "GROUP BY type " +
            "HAVING type IN ('资产', '负债', '权益', '收入', '费用')")
    List<Map<String, Object>> calculateTypeTotalBalance();

    @SelectProvider(type = AccountsSqlProvider.class, method = "buildCheckCodeUniqueSql")
    int checkCodeUnique(@Param("code") String code, @Param("id") Long id);

    class AccountsSqlProvider {

        public String buildCheckCodeUniqueSql(@Param("code") String code, @Param("id") Long id) {
            StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM accounts WHERE code = #{code}");

            if (id != null) {
                sql.append(" AND id != #{id}");
            }
            return sql.toString();
        }
    }
//
//    @Select("SELECT COUNT(1) FROM entries WHERE account_id = #{accountId}")
//    int checkAccountRelateEntries(@Param("accountId") Long accountId);

    IPage<Accounts> selectPage(Page<Accounts> page, @Param(Constants.WRAPPER) Wrapper<Accounts> queryWrapper);

    // AccountsMapper.java 中新增
    @Select(
            "SELECT " +
                    "a.id, " +
                    "a.code, " +
                    "a.name, " +
                    "a.type, " +
                    "COALESCE(SUM(CASE WHEN e.entry_type = '借方' THEN e.amount ELSE 0 END), 0) AS debit_total, " +
                    "COALESCE(SUM(CASE WHEN e.entry_type = '贷方' THEN e.amount ELSE 0 END), 0) AS credit_total " +
                    "FROM accounts a " +
                    "LEFT JOIN entries e ON a.id = e.account_id " +
                    "GROUP BY a.id, a.code, a.name, a.type " +
                    "ORDER BY a.code"
    )
    List<Map<String, Object>> selectAccountBalancesWithEntries();
}