package com.platform.common.web.filter.xss;

import com.platform.common.core.EnumUtils;
import com.platform.common.enums.YesOrNoEnum;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 读取项目相关配置
 */
@Data
public class XssConfig {

    /**
     * 过滤开关
     */
    public static YesOrNoEnum ENABLED = YesOrNoEnum.NO;

    /**
     * 过滤请求
     */
    public static List<String> DATA_LIST = new ArrayList<>();

    public void setEnabled(String enabled) {
        if (!StringUtils.isEmpty(enabled)) {
            XssConfig.ENABLED = EnumUtils.toEnum(YesOrNoEnum.class, enabled, YesOrNoEnum.NO);
        }
    }

    public void setDataList(List<String> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            XssConfig.DATA_LIST = dataList;
        }
    }

}
