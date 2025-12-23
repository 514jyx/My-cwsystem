package com.example.demo.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器（专门处理创建时间、更新时间的自动赋值）
 */
@Component // 关键：交给 Spring 管理，否则注解无效
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入数据时，自动填充「创建时间」和「初始更新时间」
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充 created_at（对应实体类的 createdAt 字段）
        this.strictInsertFill(
                metaObject,       // MyBatis-Plus 元对象（无需修改）
                "createdAt",      // 实体类中的时间字段名（必须和 Transactions/Entries 类的字段名一致）
                LocalDate.class, // 字段类型（你的实体类用的是 LocalDateTime，对应这里）
                LocalDate.now()  // 填充值：当前系统时间
        );

        // 填充 updated_at（对应实体类的 updatedAt 字段）
        this.strictInsertFill(
                metaObject,
                "updatedAt",
                LocalDate.class,
                LocalDate.now()
        );
    }

    /**
     * 更新数据时，自动填充「更新时间」
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充 updated_at（仅更新时刷新）
        this.strictUpdateFill(
                metaObject,
                "updatedAt",
                LocalDate.class,
                LocalDate.now()
        );
    }
}