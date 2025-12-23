package com.example.demo.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.entity.Accounts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 财务科目服务接口（定义科目管理核心业务方法）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
public interface IAccountsService extends IService<Accounts> {

    /**
     * 分页查询科目（支持多条件筛选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param code 科目代码（模糊查询）
     * @param name 科目名称（模糊查询）
     * @param type 科目类型（精确查询，可选值：资产/负债/权益/收入/费用）
     * @return 分页科目列表
     */


    IPage<Accounts> queryAccountPage(Integer pageNum, Integer pageSize, String code, String name, String type);

    /**
     * 获取科目树形结构（用于前端树形组件展示）
     * @return 树形结构的科目列表（父科目包含子科目）
     */
    List<Accounts> getAccountTree();

    /**
     * 计算财务概览（总资产、总负债、总权益、净资产、总收入、总费用）
     * @return 财务概览统计结果（key=统计项名称，value=金额）
     */
    Map<String, BigDecimal> calculateFinancialOverview();

    /**
     * 新增科目（包含代码唯一性校验）
     * @param accounts 科目实体（需包含 code/name/type 等必填字段）
     * @return 新增结果（true=成功，false=失败）
     * @throws RuntimeException 科目代码已存在时抛出异常
     */
    boolean addAccount(Accounts accounts);

    /**
     * 编辑科目（包含代码唯一性校验，排除自身）
     * @param accounts 科目实体（需包含 id 主键）
     * @return 编辑结果（true=成功，false=失败）
     * @throws RuntimeException 科目代码已存在时抛出异常
     */
    boolean updateAccount(Accounts accounts);

    /**
     * 删除科目（包含关联分录校验，避免数据不一致）
     * @param id 科目ID
     * @return 删除结果（true=成功，false=失败）
     * @throws RuntimeException 科目已关联分录时抛出异常
     */
    boolean removeAccountById(Long id);

    /**
     * 校验科目代码唯一性
     * @param code 科目代码
     * @param id 科目ID（编辑时传入，用于排除自身）
     * @return true=唯一，false=已存在
     */
    boolean checkCodeUnique(String code, Long id);
    /**
     * 获取所有科目的当前余额（用于凭证页面、首页等展示）
     * @return 科目余额列表，每项包含 id, code, name, type, balance (BigDecimal), direction ("借"/"贷")
     */
    List<Map<String, Object>> getAccountBalances();


}