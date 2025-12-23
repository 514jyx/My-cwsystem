package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result; // 直接使用你的Result类
import com.example.demo.system.entity.Accounts;
import com.example.demo.system.service.IAccountsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 财务科目前端控制器（提供科目树形结构、财务概览、增删改查等接口）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/system/accounts")
public class AccountsController {

    @Resource
    private IAccountsService accountsService;

    /**
     * 1. 获取科目树形结构（前端科目管理、交易选科目用）
     * 接口路径：/system/accounts/tree
     * 请求方式：GET
     */
    @GetMapping("/tree")
    public Result getAccountTree() {
        List<Accounts> accountTree = accountsService.getAccountTree();
        return Result.success(accountTree); // 无total，用success(Object data)
    }

    /**
     * 2. 财务概览统计（前端首页用：总资产、总负债、总权益等）
     * 接口路径：/system/accounts/financial-overview
     * 请求方式：GET
     */
    @GetMapping("/financial-overview")
    public Result calculateFinancialOverview() {
        Map<String, BigDecimal> overview = accountsService.calculateFinancialOverview();
        return Result.success(overview); // 统计结果无total，用success(Object data)
    }

    /**
     * 3. 分页搜索科目（多条件筛选）
     * 接口路径：/system/accounts/page
     * 请求方式：GET
     * 参数说明：pageNum=页码，pageSize=每页条数，code=科目代码（模糊），name=科目名称（模糊），type=科目类型（精确）
     */
    @GetMapping("/page")
    public Result queryAccountPage(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type) {
        IPage<Accounts> accountPage = accountsService.queryAccountPage(pageNum, pageSize, code, name, type);
        // 分页接口：data=分页数据（records），total=总记录数（适配你的Result.total字段）
        return Result.success(accountPage.getRecords(), accountPage.getTotal());
    }

    /**
     * 4. 新增科目（含代码唯一性校验）
     * 接口路径：/system/accounts/add
     * 请求方式：POST
     * 请求体：Accounts对象（code/name/type必填，parentId可选，balance可选）
     */
    @PostMapping("/add")
    public Result addAccount(@RequestBody Accounts accounts) {
        try {
            boolean success = accountsService.addAccount(accounts);
            return success ? Result.success("科目新增成功") : Result.fail("科目新增失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage()); // 捕获Service层业务异常（如代码重复）
        }
    }

    /**
     * 5. 编辑科目（含代码唯一性校验、排除自身）
     * 接口路径：/system/accounts/update
     * 请求方式：PUT
     * 请求体：Accounts对象（id/code/name/type必填）
     */
    @PutMapping("/update")
    public Result updateAccount(@RequestBody Accounts accounts) {
        try {
            boolean success = accountsService.updateAccount(accounts);
            return success ? Result.success("科目编辑成功") : Result.fail("科目编辑失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage()); // 捕获业务异常
        }
    }

    /**
     * 6. 删除科目（校验是否关联分录，关联则不允许删除）
     * 接口路径：/system/accounts/delete/{id}
     * 请求方式：DELETE
     * 参数：id=科目ID（路径参数）
     */
    @DeleteMapping("/delete/{id}")
    public Result removeAccountById(@PathVariable Long id) {
        try {
            boolean success = accountsService.removeAccountById(id);
            return success ? Result.success("科目删除成功") : Result.fail("科目删除失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage()); // 捕获业务异常（如已关联分录）
        }
    }

    /**
     * 7. 科目代码唯一性校验（前端新增/编辑时实时校验）
     * 接口路径：/system/accounts/check-code
     * 请求方式：GET
     * 参数：code=科目代码，id=科目ID（编辑时传，新增时不传）
     */
    @GetMapping("/check-code")
    public Result checkCodeUnique(
            @RequestParam String code,
            @RequestParam(required = false) Long id) {
        boolean isUnique = accountsService.checkCodeUnique(code, id);
        String msg = isUnique ? "科目代码可用" : "科目代码已存在";
        return Result.success(isUnique, null); // total传null，data=是否可用（true/false）
    }

    /**
     * 8. 根据ID查询科目（前端编辑回显用）
     * 接口路径：/system/accounts/{id}
     * 请求方式：GET
     * 参数：id=科目ID（路径参数）
     */
    @GetMapping("/{id}")
    public Result getAccountById(@PathVariable Long id) {
        Accounts accounts = accountsService.getById(id); // 复用IService原生方法
        if (accounts == null) {
            return Result.fail("科目不存在");
        }
        return Result.success(accounts); // 回显数据无total，用success(Object data)
    }
    /**
     * 9. 获取所有科目的当前余额（用于凭证页面、首页展示）
     * 接口路径：/system/accounts/balances
     * 请求方式：GET
     */
    @GetMapping("/balances")
    public Result getAccountBalances() {
        List<Map<String, Object>> balances = accountsService.getAccountBalances();
        return Result.success(balances);
    }

    @GetMapping("/list")
    public Result getAccountList() {
        List<Accounts> accountsList = accountsService.lambdaQuery()
                .orderByAsc(Accounts::getCode)
                .list();
        return Result.success(accountsList);
    }
}