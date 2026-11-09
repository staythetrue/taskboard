package com.demo.taskboard.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充时间字段。
 * 实体里用 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE) 声明,
 * 这里统一处理,业务代码不用手写 setCreatedAt。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 用 setFieldValByName 强制覆盖:strictUpdateFill 只在字段为 null 时填充,
        // 而更新场景下实体是从库里查出来的,updatedAt 已有值,不会被刷新。
        this.setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
    }
}
