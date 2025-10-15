package com.sky.annotation;

/*
 * @Author lhj
 * @Create 2025/10/15 16:55
 * Description:
 * @Version 1.0
 */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于表示某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autofill {
    // 数据库操作类型: UPDATE, INSERT
    OperationType value();
}
