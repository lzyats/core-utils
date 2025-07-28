package com.platform.common.utils;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 版本比较
 */
@Slf4j
@Component
public class VersionUtils {

    /**
     * 比较版本大小
     * <p>
     * 说明：支n位基础版本号+1位子版本号
     * 示例：1.0.2>1.0.1
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 0:相同 >0:大于 <0:小于
     */
    public static int compareTo(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        return versionStrToNum(version1) - versionStrToNum(version2);
    }

    /**
     * 版本号转换为数字
     */
    public static int versionStrToNum(String version) {
        String str = StrUtil.replace(version, ".", "");
        return Integer.valueOf(str);
    }

    /**
     * 匹配版本
     */
    public static boolean matchVersion(String version) {
        if (StringUtils.isEmpty(version)) {
            return false;
        }
        return ReUtil.isMatch("\\d{1,3}(\\.\\d{1}){2}", version);
    }

}