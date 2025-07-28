package com.platform.common.aspectj;

import com.platform.common.enums.LogTypeEnum;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AppLog {
    /**
     * 模块
     */
    String value() default "";

    /**
     * 功能
     */
    LogTypeEnum type() default LogTypeEnum.OTHER;

    /**
     * 是否保存请求的参数
     */
    boolean save() default true;
}
