package com.platform.common.web.filter.upgrade;

import com.platform.common.web.vo.LabelVo;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 读取项目相关配置
 */
@Data
public class UpgradeConfig {

    /**
     * 过滤请求
     */
    public static List<LabelVo> DATA_LIST = new ArrayList<>();

    public void setDataList(List<LabelVo> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            UpgradeConfig.DATA_LIST = dataList;
        }
    }

}
