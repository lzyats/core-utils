package com.platform.common.web.filter;

import com.platform.common.web.filter.auth.AuthConfig;
import com.platform.common.web.filter.banned.BannedConfig;
import com.platform.common.web.filter.device.DeviceConfig;
import com.platform.common.web.filter.exclude.ExcludeConfig;
import com.platform.common.web.filter.path.PathConfig;
import com.platform.common.web.filter.repeat.RepeatFilter;
import com.platform.common.web.filter.sign.SignConfig;
import com.platform.common.web.filter.upgrade.UpgradeConfig;
import com.platform.common.web.filter.version.VersionConfig;
import com.platform.common.web.filter.xss.XssConfig;
import com.platform.common.web.filter.xss.XssFilter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;

/**
 * 过滤器
 */
@Data
@Component
@ConfigurationProperties(prefix = "filter")
public class FilterConfig {

    /**
     * 地址转换
     */
    private PathConfig path;
    /**
     * xss拦截
     */
    private XssConfig xss;
    /**
     * 禁用拦截
     */
    private BannedConfig banned;
    /**
     * 签名拦截
     */
    private SignConfig sign;
    /**
     * 设备拦截
     */
    private DeviceConfig device;
    /**
     * 版本拦截
     */
    private VersionConfig version;
    /**
     * 过滤拦截
     */
    private ExcludeConfig exclude;
    /**
     * 升级日志
     */
    private UpgradeConfig upgrade;
    /**
     * 认证
     */
    private AuthConfig auth;

    @Bean
    public FilterRegistrationBean repeatFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatFilter());
        registration.addUrlPatterns("/*");
        registration.setName("repeatFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean xssFilterRegistration1() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        return registration;
    }

}
