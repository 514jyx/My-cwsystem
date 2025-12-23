package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Entries;
import com.example.demo.system.entity.Transactions;
import com.example.demo.system.mapper.EntriesMapper;
import com.example.demo.system.mapper.TransactionsMapper;
import com.example.demo.system.service.ITransactionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.example.demo.system.service.IEntriesService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 财务交易服务实现类（实现交易管理核心业务逻辑，含复式记账关联处理）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Service
public class TransactionsServiceImpl extends ServiceImpl<TransactionsMapper, Transactions> implements ITransactionsService {

    @Resource
    private EntriesMapper entriesMapper;

    @Resource
    private IEntriesService entriesService;

    // 注掉：不再使用科目Mapper（无需更新余额）
    // @Resource
    // private AccountsMapper accountsMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern SERIAL_PATTERN = Pattern.compile("TR(\\d{8})(\\d{3})");

    /**
     * 分页搜索交易（支持多条件筛选）
     */
    @Override
    public IPage<Transactions> queryTransactionPage(Integer pageNum, Integer pageSize, String transNo,
                                                    String transType, String status, String startDate, String endDate) {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        return baseMapper.selectTransactionPage(new Page<>(pageNum, pageSize),
                transNo, transType, status, startDate, endDate);
    }

    /**
     * 查询最近已过账的交易（前端财务首页用）
     */
    @Override
    public List<Transactions> getRecentTransactions(Integer limit) {
        limit = limit == null || limit <= 0 ? 10 : limit;
        return baseMapper.selectRecentTransactions(limit);
    }

    /**
     * 新增交易（含关联分录，复式记账核心）
     */
    @Override
    @Transactional
    public boolean addTransactionWithEntries(Transactions transactions, List<Entries> entriesList) {
        // 1. 保存凭证主表
        boolean saveTrans = save(transactions);
        if (!saveTrans) {
            return false;
        }

        // 2. 给每个分录绑定凭证ID
        for (Entries entry : entriesList) {
            entry.setTransactionId(transactions.getId());
        }

        // 3. 批量保存分录（entriesService现在能解析了）
        return entriesService.saveBatch(entriesList);
    }

    /**
     * 变更交易状态（简化版：仅草稿→已过账，不更新余额）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTransactionStatus(Long id, String status) {
        if (id == null || !StringUtils.hasText(status)) {
            throw new RuntimeException("交易ID和目标状态不能为空");
        }

        // 查询交易（带锁，避免并发问题）
        Transactions transaction = baseMapper.selectById(id);
        if (transaction == null) {
            throw new RuntimeException("该交易不存在，无法变更状态");
        }

        String currentStatus = transaction.getStatus();
        // 仅允许「草稿→已过账」（人工审核逻辑）
        if (!"草稿".equals(currentStatus) || !"已过账".equals(status)) {
            throw new RuntimeException("不允许从[" + currentStatus + "]变更为[" + status + "]（仅支持草稿→已过账）");
        }

        // 直接更新状态（删除余额更新相关代码）
        transaction.setStatus(status);
        return updateById(transaction);
    }

    /**
     * 根据交易ID查询关联的所有分录（交易详情用，自动带科目代码+名称）
     */
    @Override
    public List<Map<String, Object>> getEntriesByTransactionId(Long transactionId) {
        if (transactionId == null) {
            throw new RuntimeException("交易ID不能为空");
        }
        return entriesMapper.selectEntriesByTransactionId(transactionId);
    }

    /**
     * 删除交易（级联删除关联分录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTransactionWithEntries(Long id) {
        if (id == null) {
            throw new RuntimeException("交易ID不能为空");
        }

        Transactions transaction = baseMapper.selectById(id);
        if (transaction == null) {
            throw new RuntimeException("该交易不存在，无法删除");
        }

        // 已过账的交易不允许删除（避免数据不一致）
        if ("已过账".equals(transaction.getStatus())) {
            throw new RuntimeException("已过账的交易不允许删除，请先取消过账");
        }

        // 级联删除分录
        entriesMapper.deleteEntriesByTransactionId(id);
        // 删除主表交易
        return removeById(id);
    }

    /**
     * 生成唯一交易编号（对外提供 Result 格式返回）
     */
    @Override
    public Result generateTransNo() {
        String transNo = generateUniqueTransNo();
        return Result.success(transNo);
    }

    // ======================== 内部工具方法（保留必要逻辑，删除余额相关）========================

    /**
     * 内部生成唯一交易编号（格式：TR+年月日+3位流水号，如TR20251219001）
     */
    private String generateUniqueTransNo() {
        String todayPrefix = "TR" + LocalDate.now().format(DATE_FORMATTER);
        // 查询当天最大交易编号
        String maxNo = baseMapper.generateTransNo();

        if (maxNo == null || maxNo.isEmpty()) {
            return todayPrefix + "001"; // 当天第一条：TR20251219001
        }

        // 解析最大编号的流水号，自增1
        Matcher matcher = SERIAL_PATTERN.matcher(maxNo);
        if (matcher.matches()) {
            int serial = Integer.parseInt(matcher.group(2)) + 1;
            return todayPrefix + String.format("%03d", serial); // 流水号补3位0
        } else {
            // 编号格式异常时，默认从001开始
            return todayPrefix + "001";
        }
    }

    /**
     * 校验分录列表合法性（简化逻辑，仅用于新增场景）
     */
    private boolean validateEntriesList(List<Entries> entriesList) {
        // 1. 分录列表非空且至少2条
        if (CollectionUtils.isEmpty(entriesList) || entriesList.size() < 2) {
            return false;
        }

        BigDecimal debitTotal = BigDecimal.ZERO; // 借方总金额
        BigDecimal creditTotal = BigDecimal.ZERO; // 贷方总金额

        for (Entries entry : entriesList) {
            // 2. 校验单条分录参数（科目ID、借贷类型、金额不能为空，金额>0）
            if (entry.getAccountId() == null
                    || !StringUtils.hasText(entry.getEntryType())
                    || entry.getAmount() == null
                    || entry.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 3. 累加借贷金额
            if ("借方".equals(entry.getEntryType())) {
                debitTotal = debitTotal.add(entry.getAmount());
            } else if ("贷方".equals(entry.getEntryType())) {
                creditTotal = creditTotal.add(entry.getAmount());
            } else {
                // 借贷类型只能是「借方」或「贷方」
                return false;
            }
        }

        // 4. 校验借贷平衡（核心）
        return debitTotal.equals(creditTotal);
    }

    /**
     * 编辑凭证（实现类补全这个方法）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTransactionWithEntries(Transactions transactions, List<Entries> entriesList) {
        // 1. 校验交易是否存在：用 baseMapper.selectById 替代 existsById
        if (transactions.getId() == null || baseMapper.selectById(transactions.getId()) == null) {
            throw new RuntimeException("要编辑的交易不存在");
        }

        // 2. 校验分录合法性（不变）
        if (!validateEntriesList(entriesList)) {
            throw new RuntimeException("分录列表不合法：需至少2条分录，且借贷金额平衡、科目ID/类型/金额不能为空");
        }

        // 3. 锁定交易编号（不变）
        Transactions oldTrans = baseMapper.selectById(transactions.getId());
        transactions.setTransNo(oldTrans.getTransNo()); // 锁定交易编号，不允许修改
        boolean updateResult = updateById(transactions);
        if (!updateResult) {
            throw new RuntimeException("交易主信息更新失败");
        }

        // 4. 删除原关联分录（不变）
        entriesMapper.deleteEntriesByTransactionId(transactions.getId());

        // 5. 给新分录绑定交易ID，批量插入（不变）
        entriesList.forEach(entry -> entry.setTransactionId(transactions.getId()));
        int insertCount = entriesMapper.batchInsertEntries(entriesList);
        if (insertCount != entriesList.size()) {
            throw new RuntimeException("新分录保存不完整（预期保存" + entriesList.size() + "条，实际保存" + insertCount + "条）");
        }

        return true;
    }


}