package com.platform.common.tongji.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true) // 链式调用
public class TongJiVo {
    /**
     * 网站id
     */
    private String siteId;
    /**
     * accessKey
     */
    private String accessKey;
    /**
     * secretKey
     */
    private String secretKey;
    /**
     * accessToken有效期一个月
     */
    private String accessToken;
    /**
     * refreshToken有效期10年
     */
    private String refreshToken;

}
