package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.QueryPageParam;
import com.example.demo.common.Result;
import com.example.demo.system.entity.User;
import com.example.demo.system.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-05
 */
@RestController
@RequestMapping("/system/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("/list")
    public List<User> list() {
        return userService.list();
    }

    //新增
    @PostMapping("/save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    //修改
    @PostMapping("/modify")
    public boolean modify(@RequestBody User user) {
        return userService.updateById(user);
    }

    //新增或修改
    @PostMapping("/saveOrModify")
    public boolean saveOrModify(@RequestBody User user) {
        return userService.saveOrUpdate(user);
    }

    //删除
    @GetMapping("/delete")
    public boolean delete(Integer id) {
        return userService.removeById(id);
    }

    //查询（模糊/匹配）
    @PostMapping("/listP")
    public Result listP(@RequestBody User user) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(user.getName())) {
            lambdaQueryWrapper.like(User::getName, user.getName());
        }
        return Result.success(userService.list(lambdaQueryWrapper));
    }

    //分页
    @PostMapping("/listPage")
    public List<User> listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String name = (String) param.get("name");
        System.out.println("name===" + (String) param.get("name"));

        Page<User> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(User::getName, name);
        }

        IPage result = userService.page(page, lambdaQueryWrapper);
        System.out.println("total==" + result.getTotal());

        return result.getRecords();
    }

    @PostMapping("/listPage1")
    public Result listPage1(@RequestBody QueryPageParam query) {
        HashMap<String, Object> param = query.getParam();
        String name = param.get("name") != null ? param.get("name").toString().trim() : "";

        Integer roleId = -1;
        try {
            if (param.get("roleId") != null) {
                roleId = Integer.parseInt(param.get("roleId").toString());
            }
        } catch (Exception e) {
            roleId = -1;
            e.printStackTrace();
        }

        System.out.println("最终接收：name=" + name + "，roleId=" + roleId);

        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(User::getName, name);
        }

        if (roleId != -1) {
            lambdaQueryWrapper.eq(User::getRoleId, roleId);
            System.out.println("已添加角色筛选：role_id = " + roleId);
        }

        // 执行查询
        IPage<User> resultPage = userService.page(page, lambdaQueryWrapper);

        // 打印筛选结果
        System.out.println("筛选后总数：" + resultPage.getTotal());
        System.out.println("筛选后数据：" + resultPage.getRecords());

        return Result.success(resultPage.getRecords(), resultPage.getTotal());
    }

    @PostMapping("/add")
    public Result addUser(@RequestBody User user) {
        try {

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return Result.fail("密码不能为空");
            }
            if (user.getPassword().length() < 3) {
                return Result.fail("密码长度不能少于3位");
            }

            boolean save = userService.save(user);
            if (save) {
                return Result.success("新增成功");
            } else {
                return Result.fail("新增失败");
            }
        } catch (Exception e) {
            return Result.fail("新增异常：" + (e.getMessage() != null ? e.getMessage() : "未知错误"));
        }
    }

    /**
     * 检查账号是否已存在
     */
    @PostMapping("/checkAccount")
    public Result checkAccount(@RequestBody Map<String, String> param) {
        String no = param.get("no");
        if (StringUtils.isBlank(no)) {
            return Result.fail("账号不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getNo, no);
        boolean exists = userService.exists(queryWrapper);

        if (exists) {
            return Result.success("账号已存在");  // 存在返回成功状态
        } else {
            return Result.fail("账号可用");       // 不存在返回失败状态
        }
    }
    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        // 参数校验
        if (StringUtils.isBlank(username)) {
            return Result.fail("用户名不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return Result.fail("密码不能为空");
        }

        // 用户名密码校验
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getNo, username);  // 使用账号(no)作为登录用户名
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            return Result.fail("用户名不存在");
        }

        // 密码校验（实际项目中应使用加密存储和校验）
        if (!user.getPassword().equals(password)) {
            return Result.fail("密码错误");
        }

        // 生成token（实际项目中建议使用JWT等安全方式生成）
        String token = UUID.randomUUID().toString();

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        // 正确的返回方式：先设置数据，再单独设置消息
        Result result = Result.success(data);
        result.setMsg("登录成功");
        return result;
    }
}