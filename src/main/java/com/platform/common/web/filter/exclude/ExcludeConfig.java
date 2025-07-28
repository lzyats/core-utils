package com.platform.common.web.filter.exclude;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 读取项目相关配置
 */
@Data
public class ExcludeConfig {

    /**
     * 过滤请求
     */
    public static List<String> DATA_LIST = new ArrayList<>();

    public void setDataList(List<String> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            ExcludeConfig.DATA_LIST = dataList;
        }
    }

}
