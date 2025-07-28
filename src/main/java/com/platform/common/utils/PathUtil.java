package com.platform.common.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;

public class PathUtil {

    // 匹配器
    private static final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 验证地址
     */
    public static boolean verifyUrl(String currentUrl, List<String> whiteList) {
        for (String url : whiteList) {
            if (pathMatcher.match(url, currentUrl)) {
                return true;
            }
        }
        return false;
    }

}
