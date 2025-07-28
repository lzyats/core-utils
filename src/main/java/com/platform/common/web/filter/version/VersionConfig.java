package com.platform.common.web.filter.version;

import com.platform.common.core.EnumUtils;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.web.version.VersionEnum;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class VersionConfig {

    /**
     * 版本开关
     */
    public static YesOrNoEnum ENABLED = YesOrNoEnum.NO;
    /**
     * 最低版本
     */
    public static String LOWEST = VersionEnum.V1_0_0.getCode();

    public void setEnabled(String enabled) {
        if (!StringUtils.isEmpty(enabled)) {
            VersionConfig.ENABLED = EnumUtils.toEnum(YesOrNoEnum.class, enabled, YesOrNoEnum.NO);
        }
    }

    public void setLowest(String lowest) {
        if (!StringUtils.isEmpty(lowest)) {
            VersionConfig.LOWEST = lowest;
        }
    }

}
