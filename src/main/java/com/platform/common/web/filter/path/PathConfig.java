package com.platform.common.web.filter.path;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class PathConfig {

    /**
     * 接口前缀
     */
    public static String PREFIX = "/webApi";

    /**
     * 过滤请求
     */
    public static Map<String, String> DATA_LIST = new HashMap<>();

    public void setPrefix(String prefix) {
        if (!StringUtils.isEmpty(prefix)) {
            PathConfig.PREFIX = prefix;
        }
    }

    public void setDataList(Map<String, String> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            PathConfig.DATA_LIST = dataList;
        }
    }

}
