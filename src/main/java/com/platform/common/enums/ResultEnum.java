package com.platform.common.enums;

import lombok.Getter;

/**
 * 返回码枚举
 */
@Getter
public enum ResultEnum {

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),
    /**
     * 登录已过期
     */
    UNAUTHORIZED(401, "登录已过期，请重新登录"),
    /**
     * 权限不足
     */
    PERM(403, "权限不足，请联系管理员"),
    /**
     * 资源/服务未找到
     */
    NOT_FOUND(404, "路径不存在，请检查路径是否正确"),
    /**
     * 操作失败
     */
    FAIL(500, "网络开小差了，请稍后再试"),
    /**
     * 版本号
     */
    VERSION(505, "版本过低，请升级"),
    /**
     * 密码
     */
    PASS(506, "请设置密码"),
    /**
     * 禁用
     */
    BANNED(507, "用户已被限制"),
    ;

    private final Integer code;
    private final String info;

    ResultEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }

    public static ResultEnum init(Integer code) {
        for (ResultEnum resultCode : ResultEnum.values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return ResultEnum.FAIL;
    }

}
