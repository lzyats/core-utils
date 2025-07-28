package com.platform.common.aspectj;

import com.platform.common.web.version.VersionEnum;

import java.lang.annotation.*;


/**
 * 自定义一个注解，给需要版本控制的方法加上该注解
 * <p>
 * 使用：@SubmitRepeat
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VersionRepeat {

    /**
     * 当前版本
     */
    VersionEnum value();

    /**
     * 升级版本（升级版本标记使用，无任何业务意义）
     */
    VersionEnum upgrade() default VersionEnum.V1_0_0;

}
