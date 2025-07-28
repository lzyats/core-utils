package com.platform.common.web.filter.path;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.common.constant.HeadConstant;
import com.platform.common.utils.ServletUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class PathFilter extends RequestContextFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith(PathConfig.PREFIX)) {
            String currentUrl = ServletUtils.getAttribute(request, HeadConstant.REQUEST_PATH, requestPath);
            request.getRequestDispatcher(currentUrl).forward(request, response);
        } else {
            super.doFilterInternal(request, response, filterChain);
        }
    }

    /**
     * 转换地址
     */
    public static String convertPath(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        if (requestPath.startsWith(PathConfig.PREFIX)) {
            // 去掉前缀
            String currentPath = StrUtil.removePrefix(requestPath, PathConfig.PREFIX);
            // 匹配路径
            String search = ReUtil.getGroup0("/[a-zA-Z0-9]*", currentPath);
            // 获取转换
            String replace = PathConfig.DATA_LIST.get(StrUtil.removePrefix(search, "/"));
            // 替换数据
            requestPath = StrUtil.replaceFirst(currentPath, search, replace);
        }
        request.setAttribute(HeadConstant.REQUEST_PATH, requestPath);
        request.setAttribute(HeadConstant.SIGN_PATH, request.getServletPath());
        return requestPath;
    }

}