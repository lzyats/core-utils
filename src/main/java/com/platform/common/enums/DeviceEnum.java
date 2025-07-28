package com.platform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 设备类型枚举
 */
@Getter
public enum DeviceEnum {

    /**
     * 安卓
     */
    ANDROID("android", DeviceTypeEnum.PHONE),
    /**
     * iOS
     */
    IOS("ios", DeviceTypeEnum.PHONE),
    /**
     * windows
     */
    WINDOWS("windows", DeviceTypeEnum.PC),
    /**
     * macOS
     */
    MAC("mac", DeviceTypeEnum.PC),
    /**
     * h5
     */
    H5("h5", DeviceTypeEnum.H5),
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final DeviceTypeEnum deviceType;

    DeviceEnum(String code, DeviceTypeEnum deviceType) {
        this.code = code;
        this.deviceType = deviceType;
    }

    /**
     * 设备类型枚举
     */
    @Getter
    public enum DeviceTypeEnum {

        /**
         * 手机
         */
        PHONE("phone"),
        /**
         * 电脑
         */
        PC("pc"),
        /**
         * h5
         */
        H5("h5"),
        ;

        @EnumValue
        @JsonValue
        private final String code;

        DeviceTypeEnum(String code) {
            this.code = code;
        }

    }

}
