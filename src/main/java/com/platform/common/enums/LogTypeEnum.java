package com.platform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 业务操作类型
 */
@Getter
public enum LogTypeEnum {

    /**
     * 新增
     */
    ADD("1", "新增"),

    /**
     * 修改
     */
    EDIT("2", "修改"),

    /**
     * 删除
     */
    DELETE("3", "删除"),

    /**
     * 导出
     */
    EXPORT("4", "导出"),

    /**
     * 导入
     */
    IMPORT("5", "导入"),

    /**
     * 状态
     */
    STATUS("6", "状态"),

    /**
     * 置顶
     */
    TOP("7", "置顶"),

    /**
     * 查询
     */
    QUERY("8", "查询"),

    /**
     * 重置
     */
    RESET("9", "重置"),

    /**
     * 其它
     */
    OTHER("99", "其它");

    @EnumValue
    @JsonValue
    private final String code;
    private final String info;

    LogTypeEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

}
