package com.platform.common.aspectj;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import com.platform.common.enums.ResultEnum;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.exception.BaseException;
import com.platform.common.redis.RedisUtils;
import com.platform.common.shiro.ShiroUtils;
import com.platform.common.utils.ServletUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class SubmitAspect {

    private static final String DUPLICATE_KEY = "submit:";

    @Autowired(required = false)
    private RedisUtils redisUtils;

    @SneakyThrows
    @Around("execution(* com.platform..*Controller.*(..)) && @annotation(submitRepeat)")
    public Object around(ProceedingJoinPoint joinPoint, SubmitRepeat submitRepeat) {
        String cacheKey = getCacheKey(submitRepeat);
        if (redisUtils.hasKey(cacheKey)) {
            if (YesOrNoEnum.YES.equals(submitRepeat.exception())) {
                throw new BaseException(submitRepeat.msg());
            }
            throw new BaseException(ResultEnum.SUCCESS);
        }
        redisUtils.set(cacheKey, "0", submitRepeat.value(), TimeUnit.MILLISECONDS);
        return joinPoint.proceed();
    }

    /**
     * 加上用户的唯一标识
     */
    private String getCacheKey(SubmitRepeat submitRepeat) {
        StringBuilder builder = new StringBuilder(DUPLICATE_KEY);
        HttpServletRequest request = ServletUtils.getRequest();
        String param = null;
        // 获取登录参数
        if (ShiroUtils.isLogin()) {
            param = ShiroUtils.getToken();
        }
        // 获取body参数
        if (StringUtils.isEmpty(param)) {
            param = ServletUtil.getBody(request);
        }
        // 获取param参数
        if (StringUtils.isEmpty(param)) {
            Map<String, String> map = ServletUtil.getParamMap(request);
            param = JSONUtil.toJsonStr(map);
        }
        builder.append(param);
        builder.append("_");
        // 获取path参数
        String path = submitRepeat.path();
        if (StringUtils.isEmpty(path)) {
            path = request.getServletPath();
        }
        builder.append(path);
        return builder.toString();
    }

}
