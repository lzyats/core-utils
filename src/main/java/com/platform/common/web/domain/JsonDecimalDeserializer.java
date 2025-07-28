package com.platform.common.web.domain;

import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 自定义金额转换
 */
public class JsonDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        String date = jsonParser.getText();
        return parseDecimal(date);
    }

    /**
     * 金额型字符串转化为金额 格式
     */
    public static BigDecimal parseDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        String str = obj.toString();
        if (!NumberUtil.isNumber(str)) {
            return null;
        }
        return NumberUtil.toBigDecimal(str);
    }

}
