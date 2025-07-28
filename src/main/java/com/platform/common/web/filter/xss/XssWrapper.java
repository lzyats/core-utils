package com.platform.common.web.filter.xss;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * XSS过滤处理
 */
public class XssWrapper extends HttpServletRequestWrapper {

    public XssWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            for (int i = 0; i < length; i++) {
                // 防xss攻击和过滤前后空格
                values[i] = doXss(values[i]).trim();
            }
        }
        return values;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 非json类型，直接返回
        if (!isJsonRequest()) {
            return super.getInputStream();
        }

        // 为空，直接返回
        String json = IOUtils.toString(super.getInputStream(), "UTF-8");
        if (StringUtils.isEmpty(json)) {
            return super.getInputStream();
        }

        // xss过滤
        final ByteArrayInputStream bis = new ByteArrayInputStream(doXss(json).trim().getBytes("UTF-8"));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return true;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return bis.read();
            }
        };
    }

    private String doXss(String str) {
        String result = regexReplace(Pattern.compile("&"), "&amp;", str);
        result = regexReplace(Pattern.compile("<"), "&lt;", result);
        result = regexReplace(Pattern.compile(">"), "&gt;", result);
        return result;
    }

    private static String regexReplace(Pattern pattern, String replacement, String s) {
        return pattern.matcher(s).replaceAll(replacement);
    }

    /**
     * 是否是Json请求
     */
    public boolean isJsonRequest() {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(header)
                || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(header);
    }

}