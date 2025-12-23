package com.example.demo.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Entries;
import com.example.demo.system.entity.Transactions;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 财务交易服务接口（定义交易管理核心业务方法，含复式记账关联逻辑）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface ITransactionsService extends IService<Transactions> {

    /**
     * 分页搜索交易（支持多条件筛选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param transNo 交易编号（模糊查询）
     * @param transType 交易类型（精确查询：采购/销售/收款/付款/费用/收入/转账）
     * @param status 交易状态（精确查询：草稿/已过账/已取消）
     * @param startDate 交易日期起始（格式：yyyy-MM-dd）
     * @param endDate 交易日期结束（格式：yyyy-MM-dd）
     * @return 分页交易列表
     */
    IPage<Transactions> queryTransactionPage(Integer pageNum, Integer pageSize, String transNo,
                                             String transType, String status, String startDate, String endDate);

    /**
     * 查询最近已过账的交易（前端财务首页用）
     * @param limit 查询条数（默认10条）
     * @return 最近交易列表（按交易日期倒序）
     */
    List<Transactions> getRecentTransactions(Integer limit);

    /**
     * 新增交易（含关联分录，复式记账核心）
     * @param transactions 交易主信息
     * @param entriesList 交易关联的分录列表（至少2条，确保借贷平衡）
     * @return 新增结果（true=成功，false=失败）
     * @throws RuntimeException 借贷不平衡、必填字段缺失时抛出异常
     */
    boolean addTransactionWithEntries(Transactions transactions, List<Entries> entriesList);

    /**
     * 变更交易状态（草稿→已过账、已过账→已取消等）
     * @param id 交易ID
     * @param status 目标状态（草稿/已过账/已取消）
     * @return 变更结果（true=成功，false=失败）
     * @throws RuntimeException 交易不存在、状态变更不合法时抛出异常
     */
    boolean updateTransactionStatus(Long id, String status);

    /**
     * 根据交易ID查询关联的所有分录（交易详情用）
     * @param transactionId 交易ID
     * @return 分录列表（含科目名称、科目代码）
     */
    List<Map<String, Object>> getEntriesByTransactionId(Long transactionId);

    /**
     * 删除交易（级联删除关联分录）
     * @param id 交易ID
     * @return 删除结果（true=成功，false=失败）
     * @throws RuntimeException 交易不存在时抛出异常
     */
    boolean removeTransactionWithEntries(Long id);

    boolean updateTransactionWithEntries(Transactions transactions, List<Entries> entriesList);

    /**
     * 生成唯一交易编号（新增交易时自动调用）
     * @return 交易编号（格式：TR+年月日+3位流水号，如TR20251215001）
     */
    Result generateTransNo();
}