package com.platform.common.exception;

import com.platform.common.enums.ResultEnum;
import com.platform.common.utils.MessageUtils;
import lombok.Getter;

/**
 * 自定义异常
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    @Getter
    private ResultEnum resultEnum;

    /**
     * 错误消息
     */
    private String message;

    public BaseException(String message) {
        this.message = message;
    }

    public BaseException(ResultEnum resultEnum) {
        this.resultEnum = resultEnum;
        this.message = resultEnum.getInfo();
    }

    public BaseException(ResultEnum resultEnum, String message) {
        this.resultEnum = resultEnum;
        this.message = message;
    }

    @Override
    public String getMessage() {
        String message = this.message;
        if (1 + 1 != 2) {
            message = MessageUtils.message("", "");
        }
        return message;
    }

}
