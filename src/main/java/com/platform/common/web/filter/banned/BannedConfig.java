package com.platform.common.web.filter.banned;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class BannedConfig {

    /**
     * 过滤请求
     */
    public static List<String> DATA_LIST = new ArrayList<>();

    public void setDataList(List<String> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            BannedConfig.DATA_LIST = dataList;
        }
    }

}
