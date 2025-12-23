package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.system.entity.Suppliers;
import com.example.demo.system.service.ISuppliersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/system/suppliers")

@Api(tags = "供应商管理接口")
public class SuppliersController {

    @Autowired
    private ISuppliersService suppliersService;

    @GetMapping("/list") // 接口路径：/system/suppliers/list（前端请求的就是这个）
    @ApiOperation("查询所有供应商列表（供订单下拉框使用）")
    public Map<String, Object> getAllSuppliers() { // 返回格式适配前端
        List<Suppliers> suppliersList = suppliersService.list(); // 查所有供应商（MyBatis-Plus自带方法）

        // 前端需要 code=200 + data=列表，所以按这个格式返回
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200); // 前端判断成功的关键
        result.put("data", suppliersList); // 供应商列表数据
        return result;
    }

    /**
     * 分页查询供应商
     */
    @GetMapping("/page")
    @ApiOperation("分页查询供应商")
    public Map<String, Object> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name
    ) {
        Page<Suppliers> page = new Page<>(pageNum, pageSize);
        IPage<Suppliers> suppliersPage = suppliersService.getPage(page, name);

        Map<String, Object> result = new HashMap<>();
        result.put("total", suppliersPage.getTotal());
        result.put("data", suppliersPage.getRecords());
        return result;
    }

    /**
     * 根据ID查询供应商
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询供应商")
    public Suppliers getById(@PathVariable Long id) {
        return suppliersService.getById(id);
    }

    /**
     * 新增供应商
     */
    @PostMapping
    @ApiOperation("新增供应商")
    public boolean save(@RequestBody Suppliers suppliers) {
        return suppliersService.saveSuppliers(suppliers);
    }

    /**
     * 更新供应商
     */
    @PutMapping
    @ApiOperation("更新供应商")
    public boolean update(@RequestBody Suppliers suppliers) {
        return suppliersService.updateSuppliers(suppliers);
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除供应商")
    public boolean delete(@PathVariable Long id) {
        return suppliersService.removeSuppliers(id);
    }
}