package com.platform.common.web.handler;

import cn.hutool.core.util.StrUtil;
import com.platform.common.constant.HeadConstant;
import com.platform.common.enums.ResultEnum;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.exception.BaseException;
import com.platform.common.utils.PathUtil;
import com.platform.common.utils.VersionUtils;
import com.platform.common.web.filter.exclude.ExcludeConfig;
import com.platform.common.web.filter.version.VersionConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Getter
public class VersionHandlerCondition implements RequestCondition<VersionHandlerCondition> {

    private String version;

    public VersionHandlerCondition(String version) {
        this.version = version;
    }

    //将不同的筛选条件合并,这里采用的覆盖，即后来的规则生效
    @Override
    public VersionHandlerCondition combine(VersionHandlerCondition other) {
        return new VersionHandlerCondition(other.getVersion());
    }

    //根据request查找匹配到的筛选条件
    @Override
    public VersionHandlerCondition getMatchingCondition(HttpServletRequest request) {
        // 版本开关
        if (YesOrNoEnum.NO.equals(VersionConfig.ENABLED)) {
            return this;
        }
        String currentUrl = request.getServletPath();
        if (PathUtil.verifyUrl(currentUrl, ExcludeConfig.DATA_LIST)) {
            return this;
        }
        String version = request.getHeader(HeadConstant.VERSION);
        String uri = request.getRequestURI();
        if (StrUtil.startWith(uri, "/error")) {
            return null;
        }
        if (!VersionUtils.matchVersion(version)) {
            log.error("RequestURI：" + uri);
            log.error("传入版本格式有误version-{}", version);
            throw new BaseException(ResultEnum.VERSION);
        }
        // 验证最低
        if (VersionUtils.compareTo(version, VersionConfig.LOWEST) < 0) {
            throw new BaseException(ResultEnum.VERSION);
        }
        if (VersionUtils.compareTo(version, this.version) >= 0) {
            return this;
        }
        return null;
    }

    //实现不同条件类的比较，从而实现优先级排序,返回值最小匹配this
    @Override
    public int compareTo(VersionHandlerCondition other, HttpServletRequest request) {
        return VersionUtils.compareTo(other.getVersion(), this.version);
    }

}