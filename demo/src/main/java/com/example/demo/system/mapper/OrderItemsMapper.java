package com.example.demo.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.system.entity.OrderItems; // 改为 OrderItems（你的实体类）
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 订单项 Mapper 接口（对应数据库表 order_items）
 * 功能：订单项的查询、删除等操作（关联订单管理）
 *
 * @author jyxmn
 * @since 2025-12-22
 */
@Repository
public interface OrderItemsMapper extends BaseMapper<OrderItems> { // 泛型改为 OrderItems

    /**
     * 根据订单ID查询关联的所有订单项
     * 用途：订单详情展示、编辑订单时回显订单项
     *
     * @param orderId 订单主键ID（orders表的id）
     * @return 该订单下的所有订单项列表（按id升序）
     */
    List<OrderItems> selectByOrderId(@Param("orderId") Long orderId); // 返回值改为 List<OrderItems>

    /**
     * 根据订单ID批量删除关联的订单项
     * 用途：编辑订单时删除原有订单项、删除订单时同步删除订单项
     *
     * @param orderId 订单主键ID（orders表的id）
     * @return 受影响的行数（删除的订单项数量）
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}