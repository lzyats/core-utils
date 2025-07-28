package com.platform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 渠道类型枚举
 */
@Getter
public enum ChannelEnum {

    /**
     * 正常消息
     */
    MSG("msg"),
    /**
     * 扫码消息
     */
    SCAN("scan"),
    /**
     * 全员消息
     */
    ALL("all"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    ChannelEnum(String code) {
        this.code = code;
    }

}
