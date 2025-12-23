package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Customers;
import com.example.demo.system.service.ICustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils; // 新增：用于判断字符串是否为空
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 新增：用于条件查询

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */

@RestController
@RequestMapping("/system/customers")
public class CustomersController {

    @Autowired
    private ICustomersService customersService;

    // 新增客户
    @PostMapping
    public boolean add(@RequestBody Customers customers) {
        return customersService.save(customers);
    }

    // 删除客户
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return customersService.removeById(id);
    }

    // 更新客户
    @PutMapping
    public boolean update(@RequestBody Customers customers) {
        return customersService.updateById(customers);
    }

    // 查询单个客户
    @GetMapping("/{id}")
    public Customers getById(@PathVariable Long id) {
        return customersService.getById(id);
    }

    // 分页查询所有客户（关键修改2：支持name模糊搜索，和前端匹配）
    @GetMapping("/page")
    public IPage<Customers> getPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name // 新增：接收前端的搜索关键词，非必填
    ) {
        IPage<Customers> page = new Page<>(pageNum, pageSize);
        // 条件查询：如果name不为空，按名称模糊搜索
        QueryWrapper<Customers> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name); // 数据库字段name，模糊匹配
        }
        return customersService.page(page, queryWrapper); // 带条件分页查询
    }

    // 自定义查询：查询余额大于指定值的客户
    @GetMapping("/balance/{minBalance}")
    public List<Customers> getByBalance(@PathVariable BigDecimal minBalance) {
        return customersService.getByBalanceGreaterThan(minBalance);
    }

    // 新增：全量客户列表接口（供前端销售单下拉框使用）
    @GetMapping("/list")
    public Result listAll() {
        try {
            List<Customers> customersList = customersService.listAllCustomers();
            // 用项目Result类返回：code=200，msg=成功，data=客户列表
            return Result.success(customersList);
        } catch (Exception e) {
            // 异常捕获：返回失败信息
            return Result.fail("拉取客户列表失败：" + e.getMessage());
        }
    }
}