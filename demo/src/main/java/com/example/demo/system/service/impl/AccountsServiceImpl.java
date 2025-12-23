package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Accounts;
import com.example.demo.system.mapper.AccountsMapper;
import com.example.demo.system.service.IAccountsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 财务科目服务实现类（阶段1：仅科目树功能，禁用分录关联校验）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Service
public class AccountsServiceImpl extends ServiceImpl<AccountsMapper, Accounts> implements IAccountsService {

    /**
     * 分页查询科目（多条件筛选）
     */
    @Override
    public IPage<Accounts> queryAccountPage(Integer pageNum, Integer pageSize, String code, String name, String type) {
        // 1. 校验分页参数（避免传入 null 导致报错）
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        // 2. 调用 Mapper 分页方法（通过 baseMapper 调用，避免注入冲突）
        return baseMapper.selectAccountPage(new Page<>(pageNum, pageSize), code, name, type);
    }

    /**
     * 构建科目树形结构（核心逻辑：父级包含子级）
     */
    @Override
    public List<Accounts> getAccountTree() {
        // 1. 查询所有科目（按代码排序，保证树形结构顺序一致）
        List<Accounts> allAccounts = baseMapper.selectAllAccounts();
        if (allAccounts == null || allAccounts.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 按父级 ID 分组（key=parentId，value=该父级下的所有子科目）
        Map<Long, List<Accounts>> childMap = allAccounts.stream()
                .filter(account -> account.getParentId() != null) // 过滤子科目（parentId 不为 null）
                .collect(Collectors.groupingBy(Accounts::getParentId));

        // 3. 递归设置子科目（从顶级科目开始，层层嵌套）
        List<Accounts> rootAccounts = allAccounts.stream()
                .filter(account -> account.getParentId() == null) // 顶级科目（parentId 为 null）
                .map(root -> {
                    setChildren(root, childMap);
                    return root;
                })
                .collect(Collectors.toList());

        return rootAccounts;
    }

    /**
     * 递归给父科目设置子科目（树形结构辅助方法）
     */
    private void setChildren(Accounts parent, Map<Long, List<Accounts>> childMap) {
        // 获取当前父科目的所有子科目
        List<Accounts> children = childMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children); // 设置子科目列表
            // 递归给子科目设置它们的子科目（支持多级层级）
            children.forEach(child -> setChildren(child, childMap));
        }
    }

    /**
     * 计算财务概览（总资产、总负债、总权益、净资产、总收入、总费用）
     */
    @Override
    public Map<String, BigDecimal> calculateFinancialOverview() {
        Map<String, BigDecimal> overview = new HashMap<>(6);
        // 1. 从 Mapper 获取按类型统计的总余额（通过 baseMapper 调用）
        List<Map<String, Object>> typeTotalList = baseMapper.calculateTypeTotalBalance();

        // 2. 初始化所有统计项为 0.00（避免 null 导致前端报错）
        BigDecimal totalAsset = BigDecimal.ZERO;    // 总资产
        BigDecimal totalLiability = BigDecimal.ZERO;// 总负债
        BigDecimal totalEquity = BigDecimal.ZERO;   // 总权益
        BigDecimal totalIncome = BigDecimal.ZERO;   // 总收入
        BigDecimal totalExpense = BigDecimal.ZERO;  // 总费用

        // 3. 按科目类型累加余额
        for (Map<String, Object> item : typeTotalList) {
            String type = (String) item.get("type");
            BigDecimal totalBalance = (BigDecimal) item.get("total_balance");
            if (totalBalance == null) {
                totalBalance = BigDecimal.ZERO;
            }

            switch (type) {
                case "资产":
                    totalAsset = totalAsset.add(totalBalance);
                    break;
                case "负债":
                    totalLiability = totalLiability.add(totalBalance);
                    break;
                case "权益":
                    totalEquity = totalEquity.add(totalBalance);
                    break;
                case "收入":
                    totalIncome = totalIncome.add(totalBalance);
                    break;
                case "费用":
                    totalExpense = totalExpense.add(totalBalance);
                    break;
                default:
                    break;
            }
        }

        // 4. 计算净资产（净资产 = 总资产 - 总负债 + 总权益）
        BigDecimal netAsset = totalAsset.subtract(totalLiability).add(totalEquity);

        // 5. 封装结果（key 与前端一致，便于前端取值）
        overview.put("totalAsset", totalAsset);
        overview.put("totalLiability", totalLiability);
        overview.put("totalEquity", totalEquity);
        overview.put("netAsset", netAsset);
        overview.put("totalIncome", totalIncome);
        overview.put("totalExpense", totalExpense);

        return overview;
    }

    /**
     * 新增科目（含代码唯一性校验）
     */
    @Override
    public boolean addAccount(Accounts accounts) {
        // 1. 校验必填字段
        if (!validateRequiredFields(accounts)) {
            throw new RuntimeException("科目代码、名称、类型不能为空");
        }

        // 2. 校验科目代码唯一性（新增时 id 为 null，无需排除自身）
        if (!checkCodeUnique(accounts.getCode(), null)) {
            throw new RuntimeException("科目代码已存在，请更换");
        }

        // 3. 初始化余额（未传入时默认为 0.00）
        if (accounts.getBalance() == null) {
            accounts.setBalance(BigDecimal.ZERO);
        }

        // 4. 调用 MyBatis-Plus 原生方法保存
        return save(accounts);
    }

    /**
     * 编辑科目（含代码唯一性校验，排除自身）
     */
    @Override
    public boolean updateAccount(Accounts accounts) {
        // 1. 校验主键和必填字段
        if (accounts.getId() == null || !validateRequiredFields(accounts)) {
            throw new RuntimeException("科目ID、代码、名称、类型不能为空");
        }

        // 2. 校验科目是否存在
        Accounts existAccount = baseMapper.selectById(accounts.getId());
        if (existAccount == null) {
            throw new RuntimeException("该科目不存在，无法编辑");
        }

        // 3. 校验科目代码唯一性（编辑时排除自身 ID）
        if (!checkCodeUnique(accounts.getCode(), accounts.getId())) {
            throw new RuntimeException("科目代码已存在，请更换");
        }

        // 4. 调用 MyBatis-Plus 原生方法更新
        return updateById(accounts);
    }

    /**
     * 删除科目（阶段1：仅校验子科目，禁用分录关联校验）
     */
    @Override
    public boolean removeAccountById(Long id) {
        // 1. 校验 ID
        if (id == null) {
            throw new RuntimeException("科目ID不能为空");
        }

        // 2. 校验科目是否存在
        Accounts existAccount = baseMapper.selectById(id);
        if (existAccount == null) {
            throw new RuntimeException("该科目不存在，无法删除");
        }

        // 3. 阶段1：注释分录关联校验（entries 表未使用，避免报错）
        // int entryCount = baseMapper.checkAccountRelateEntries(id);
        // if (entryCount > 0) {
        //     throw new RuntimeException("该科目已关联" + entryCount + "笔交易分录，无法删除");
        // }

        // 4. 校验是否有子科目（有子科目不允许删除）
        List<Accounts> children = lambdaQuery().eq(Accounts::getParentId, id).list();
        if (children != null && !children.isEmpty()) {
            throw new RuntimeException("该科目包含子科目，请先删除子科目");
        }

        // 5. 调用 MyBatis-Plus 原生方法删除
        return removeById(id);
    }

    /**
     * 校验科目代码唯一性
     */
    @Override
    public boolean checkCodeUnique(String code, Long id) {
        if (!StringUtils.hasText(code)) {
            return false; // 代码为空，不唯一
        }
        // 调用 Mapper 方法查询相同代码的科目数量（修复后支持动态 SQL）
        int count = baseMapper.checkCodeUnique(code, id);
        return count == 0; // count=0 表示唯一
    }

    /**
     * 校验科目必填字段（code/name/type）
     */
    private boolean validateRequiredFields(Accounts accounts) {
        return StringUtils.hasText(accounts.getCode())
                && StringUtils.hasText(accounts.getName())
                && StringUtils.hasText(accounts.getType());
    }

    /**
     * 获取所有科目的当前余额（用于凭证页面、首页等展示）
     */
    @Override
    public List<Map<String, Object>> getAccountBalances() {
        // 直接调用新方法，不再使用 QueryWrapper.apply()
        List<Map<String, Object>> resultList = baseMapper.selectAccountBalancesWithEntries();

        // 后续计算 balance 和 direction 的逻辑保持不变
        for (Map<String, Object> row : resultList) {
            BigDecimal debitTotal = (BigDecimal) row.get("debit_total");
            BigDecimal creditTotal = (BigDecimal) row.get("credit_total");
            String type = (String) row.get("type");

            debitTotal = debitTotal != null ? debitTotal : BigDecimal.ZERO;
            creditTotal = creditTotal != null ? creditTotal : BigDecimal.ZERO;

            BigDecimal balance;
            String direction;

            if ("资产".equals(type) || "费用".equals(type)) {
                balance = debitTotal.subtract(creditTotal);
                direction = balance.compareTo(BigDecimal.ZERO) >= 0 ? "借" : "贷";
            } else {
                balance = creditTotal.subtract(debitTotal);
                direction = balance.compareTo(BigDecimal.ZERO) >= 0 ? "贷" : "借";
            }

            row.put("balance", balance.abs());
            row.put("direction", direction);
        }

        return resultList;
    }


}