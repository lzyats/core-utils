package com.platform.common.utils;



import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * 不同类型格式转Redis工具类
 *
 * @author WangFan
 * @version 1.1 (GitHub文档: https://github.com/whvcse/RedisUtil )
 * @date 2018-02-24 下午03:09:50
 */

@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
public class ToredisUtils {

    // 初始化Gson（禁用HTML转义，避免额外字符）
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableHtmlEscaping() // 关键：避免特殊字符被转义
            .serializeNulls()
            .create();

    /**
     * 清理JSON字符串中的非法字符（解决核心错误）
     */
    private String cleanJsonString(String json) {
        if (json == null) {
            return null;
        }
        // 1. 去除前后空格
        json = json.trim();
        // 2. 去除首尾可能的引号（例如："{...}" → {...}）
        if (json.startsWith("\"") && json.endsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }
        // 3. 去除可能的转义字符（例如：\\" → "）
        json = json.replace("\\\"", "\"");
        return json;
    }
}
