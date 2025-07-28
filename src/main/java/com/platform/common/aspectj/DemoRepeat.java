package com.platform.common.aspectj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义一个注解，给需要演示的接口方法加上该注解
 * <p>
 * 使用：@DemoRepeat
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DemoRepeat {

    /**
     * 提示语
     */
    String value() default "演示模式，禁止操作";

}
