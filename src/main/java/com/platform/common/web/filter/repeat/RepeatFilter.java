package com.platform.common.web.filter.repeat;

import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Repeatable 过滤器
 */
public class RepeatFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(request.getContentType())
                || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(request.getContentType())) {
            if (request instanceof HttpServletRequest) {
                if (((HttpServletRequest) request).getMethod().matches("POST")) {
                    chain.doFilter(new RepeatWrapper((HttpServletRequest) request, response), response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

}
