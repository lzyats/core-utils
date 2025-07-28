package com.platform.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审批状态
 */
@Getter
public enum ApproveEnum {

    /**
     * 未审批
     */
    NONE("0", "未审批"),
    /**
     * 审核中
     */
    APPLY("1", "审批中"),
    /**
     * 通过
     */
    PASS("2", "已通过"),
    /**
     * 拒绝
     */
    REJECT("3", "已拒绝"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String info;

    ApproveEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

}
