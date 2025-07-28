package com.platform.common.web.filter.xss;

import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.utils.PathUtil;
import com.platform.common.web.filter.path.PathFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 防止XSS攻击的过滤器
 */
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        // 执行请求
        if (handleExclude(req)) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new XssWrapper((HttpServletRequest) request), response);
        }

    }

    private boolean handleExclude(HttpServletRequest request) {
        // 转换请求
        String currentUrl = PathFilter.convertPath(request);
        // 验证配置
        if (YesOrNoEnum.NO.equals(XssConfig.ENABLED)) {
            return true;
        }
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || method.matches("GET")) {
            return true;
        }
        return PathUtil.verifyUrl(currentUrl, XssConfig.DATA_LIST);
    }

}