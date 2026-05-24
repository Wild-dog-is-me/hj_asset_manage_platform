package com.ruoyi.framework.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ruoyi.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    public static final String CREATE_BY = "createBy";
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_BY = "updateBy";
    public static final String UPDATE_TIME = "updateTime";
    public static final String DELETED = "delFlag";

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 起始版本 3.3.0(推荐使用)
        this.setFieldValByName(CREATE_BY, SecurityUtils.getUsername(), metaObject);
        this.setFieldValByName(CREATE_TIME, formatDate(metaObject.getSetterType(CREATE_TIME)), metaObject);
        this.setFieldValByName(UPDATE_BY, SecurityUtils.getUsername(), metaObject);
        this.setFieldValByName(UPDATE_TIME, formatDate(metaObject.getSetterType(CREATE_TIME)), metaObject);
        this.setFieldValByName(DELETED, "0", metaObject);
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(UPDATE_BY, SecurityUtils.getUsername(), metaObject);
        this.setFieldValByName(UPDATE_TIME, formatDate(metaObject.getSetterType(CREATE_TIME)), metaObject);
    }


    /**
     * 处理特殊日期
     *
     * @param setterType 参数类型
     * @return 日期类型
     */
    private Object formatDate(Class<?> setterType) {
        if (Date.class.equals(setterType)) {
            return new Date();
        } else if (LocalDateTime.class.equals(setterType)) {
            return LocalDateTime.now();
        } else if (Long.class.equals(setterType)) {
            return System.currentTimeMillis();
        }
        return null;
    }
}
