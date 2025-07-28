package com.platform.common.web.version;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 版本枚举值
 */
@Getter
public enum VersionEnum {

    V1_0_0("1.0.0"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    VersionEnum(String code) {
        this.code = code;
    }

}
