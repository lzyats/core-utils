package com.platform.common.web.domain;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class FileVo {

    private String name;

    private String url;

    public static String transform(String content) {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        return JSONUtil.toBean(JSONUtil.parseArray(content).getJSONObject(0), FileVo.class).getUrl();
    }

}
