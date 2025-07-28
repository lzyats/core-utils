package com.platform.common.web.filter.sign;

import com.platform.common.core.EnumUtils;
import com.platform.common.enums.YesOrNoEnum;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class SignConfig {

    /**
     * 签名开关
     */
    public static YesOrNoEnum ENABLED = YesOrNoEnum.NO;

    /**
     * appId
     */
    public static String APP_ID = "2024";

    /**
     * 前缀
     */
    public static String PREFIX = "_";

    /**
     * 秘钥
     */
    public static String SECRET = "3ec1d37fe93b8cb2";

    public void setEnabled(String enabled) {
        if (!StringUtils.isEmpty(enabled)) {
            SignConfig.ENABLED = EnumUtils.toEnum(YesOrNoEnum.class, enabled, YesOrNoEnum.NO);
        }
    }

    public void setAppId(String appId) {
        if (!StringUtils.isEmpty(appId)) {
            SignConfig.APP_ID = appId;
        }
    }

    public void setSecret(String secret) {
        if (!StringUtils.isEmpty(secret)) {
            SignConfig.SECRET = secret;
        }
    }

    public void setPrefix(String prefix) {
        if (!StringUtils.isEmpty(prefix)) {
            SignConfig.PREFIX = prefix;
        }
    }

}
