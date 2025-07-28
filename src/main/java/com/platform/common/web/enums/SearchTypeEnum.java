package com.platform.common.web.enums;

import lombok.Getter;

/**
 * 动态查询枚举值
 */
@Getter
public enum SearchTypeEnum {

    EQ("eq"),
    GE("ge"),
    GT("gt"),
    LE("le"),
    LT("lt"),
    NE("ne"),
    BETWEEN("bt"),
    NOT_BETWEEN("nbt"),
    LIKE("lk"),
    NOT_LIKE("nlk"),
    LIKE_LEFT("lkl"),
    LIKE_RIGHT("lkr"),
    ;

    private String code;

    // 构造方法
    SearchTypeEnum(String code) {
        this.code = code;
    }

}
