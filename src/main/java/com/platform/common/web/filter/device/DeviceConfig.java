package com.platform.common.web.filter.device;

import com.platform.common.core.EnumUtils;
import com.platform.common.enums.YesOrNoEnum;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class DeviceConfig {

    /**
     * 设备开关
     */
    public static YesOrNoEnum ENABLED = YesOrNoEnum.NO;

    public void setEnabled(String enabled) {
        if (!StringUtils.isEmpty(enabled)) {
            DeviceConfig.ENABLED = EnumUtils.toEnum(YesOrNoEnum.class, enabled, YesOrNoEnum.NO);
        }
    }

}
