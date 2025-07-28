package com.platform.common.web.interceptor;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import com.platform.common.constant.HeadConstant;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.utils.PathUtil;
import com.platform.common.utils.ServletUtils;
import com.platform.common.web.domain.AjaxResult;
import com.platform.common.web.filter.exclude.ExcludeConfig;
import com.platform.common.web.filter.sign.SignConfig;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 签名拦截器
 */
@Component
public class SignInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 签名开关
        if (YesOrNoEnum.NO.equals(SignConfig.ENABLED)) {
            return true;
        }
        // 过滤拦截
        String currentUrl = request.getServletPath();
        if (PathUtil.verifyUrl(currentUrl, ExcludeConfig.DATA_LIST)) {
            return true;
        }
        String appId = ServletUtil.getHeader(request, HeadConstant.APP_ID, CharsetUtil.UTF_8);
        String timestamp = ServletUtil.getHeader(request, HeadConstant.TIMESTAMP, CharsetUtil.UTF_8);
        String sign = ServletUtil.getHeader(request, HeadConstant.SIGN, CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(appId)
                || StringUtils.isEmpty(timestamp)
                || StringUtils.isEmpty(sign)) {
            return error(response);
        }
        if (!NumberUtil.isLong(timestamp)) {
            return error(response);
        }
        if (!SignConfig.APP_ID.equals(appId)) {
            return error(response);
        }
        String requestPath = requestPath(request);
        String data = SecureUtil.md5(appId + requestPath + timestamp);
        String digest = SecureUtil.hmacMd5(SignConfig.SECRET).digestHex(data);
        // 返回
        if (!digest.equals(digest)) {
            return error(response);
        }
        return true;
    }

    /**
     * 获取参数
     */
    private String requestPath(HttpServletRequest request) {
        // 获取签名路径
        String requestPath = ServletUtils.getAttribute(request, HeadConstant.SIGN_PATH, request.getRequestURI());
        // 移除指定前缀
        requestPath = StrUtil.removePrefix(requestPath, SignConfig.PREFIX);
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String query = request.getQueryString();
            if (!StringUtils.isEmpty(query)) {
                requestPath += "?" + URLUtil.decode(query, "UTF-8");
            }
        }
        return requestPath;
    }

    @SneakyThrows
    private boolean error(HttpServletResponse response) {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().print(JSONUtil.toJsonStr(AjaxResult.fail("请求签名不正确")));
        return false;
    }

}
