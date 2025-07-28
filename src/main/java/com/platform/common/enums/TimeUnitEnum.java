/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.platform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 时间枚举
 */
@Getter
public enum TimeUnitEnum {

    /**
     * 秒
     */
    SECOND("second", "秒"),
    /**
     * 分
     */
    MINUTE("minute", "分"),
    /**
     * 时
     */
    HOUR("hour", "时"),
    /**
     * 日
     */
    DAY("day", "日"),
    /**
     * 月
     */
    MONTH("month", "月"),
    /**
     * 年
     */
    YEAR("year", "年"),

    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String info;

    TimeUnitEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }


}
