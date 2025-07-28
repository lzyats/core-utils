package com.platform.common.ehcache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 监听程序
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "ehcache")
public class EhcacheListener implements DisposableBean {

    @Resource
    private EhcacheUtils cacheUtils;

    @Override
    public void destroy() {
        log.info("====================ehcache写入磁盘====================");
        cacheUtils.shutDownCache();
    }

}
