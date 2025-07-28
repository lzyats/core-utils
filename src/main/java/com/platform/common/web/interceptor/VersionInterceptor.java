package com.platform.common.web.interceptor;

import cn.hutool.json.JSONUtil;
import com.platform.common.constant.HeadConstant;
import com.platform.common.enums.ResultEnum;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.utils.PathUtil;
import com.platform.common.utils.VersionUtils;
import com.platform.common.web.domain.AjaxResult;
import com.platform.common.web.filter.exclude.ExcludeConfig;
import com.platform.common.web.filter.version.VersionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 版本验证
 */
@Component
@Slf4j
public class VersionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (YesOrNoEnum.NO.equals(VersionConfig.ENABLED)) {
            return true;
        }
        /**
         * 如果不属于HandlerMethod，则放行
         */
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String currentUrl = request.getServletPath();
        if (PathUtil.verifyUrl(currentUrl, ExcludeConfig.DATA_LIST)) {
            return true;
        }
        String version = request.getHeader(HeadConstant.VERSION);
        // 版本格式校验
        if (!VersionUtils.matchVersion(version)) {
            return error(response);
        }
        return true;
    }

    private boolean error(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().print(JSONUtil.toJsonStr(AjaxResult.result(ResultEnum.VERSION)));
        return false;
    }

}