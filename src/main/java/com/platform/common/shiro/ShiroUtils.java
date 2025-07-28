
package com.platform.common.shiro;

import com.platform.common.constant.HeadConstant;
import com.platform.common.utils.ServletUtils;

/**
 * Shiro工具类
 */
public class ShiroUtils {

    /**
     * 是否登录
     */
    public static boolean isLogin() {
        return false;
    }

    /**
     * 获取getToken
     */
    public static String getToken() {
        return ServletUtils.getRequest().getHeader(HeadConstant.TOKEN_HEADER_ADMIN);
    }

}
