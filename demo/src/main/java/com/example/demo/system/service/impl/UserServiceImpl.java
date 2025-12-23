package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.system.entity.User;
import com.example.demo.system.mapper.UserMapper;
import com.example.demo.system.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}

