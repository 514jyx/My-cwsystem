package com.example.demo.system.service.impl;

import com.example.demo.system.entity.OrderItems;
import com.example.demo.system.mapper.OrderItemsMapper;
import com.example.demo.system.service.IOrderItemsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@Service
public class OrderItemsServiceImpl extends ServiceImpl<OrderItemsMapper, OrderItems> implements IOrderItemsService {

}
