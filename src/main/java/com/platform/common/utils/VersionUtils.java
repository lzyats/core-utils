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
     * 匹配 x.y.z 格式的版本号（支持小版本号多位数字，如 1.1.10、2.0.300）
     * 格式规则：
     * 1. 主版本号（x）：1-3位数字（如 1、99、255）
     * 2. 次版本号（y）：1位及以上数字（如 1、10、123）
     * 3. 修订版本号（z）：1位及以上数字（如 1、10、999）
     * @param version 待匹配的版本号字符串
     * @return 匹配成功返回 true，否则返回 false
     */
    public static boolean matchVersion(String version) {
        if (StrUtil.isEmpty(version)) {
            return false;
        }
        // 正则修改点：将 \\d{1}（固定1位）改为 \\d+（1位及以上）
        String versionRegex = "^\\d{1,3}(\\.\\d+){2}$";
        return ReUtil.isMatch(versionRegex, version);
    }

}