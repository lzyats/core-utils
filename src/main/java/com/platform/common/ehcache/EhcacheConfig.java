package com.platform.common.ehcache;

import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "ehcache")
public class EhcacheConfig {

    @Value("${platform.ehcachePath:C:/cache}")
    private String ehcachePath;

    @Bean(name = "ehcacheManager")
    @Qualifier("ehcacheManager")
    public CacheManager ehcacheManager() {
        System.setProperty("net.sf.ehcache.enableShutdownHook", "true");
        System.setProperty("ehcachePath", ehcachePath);
        return CacheManager.create();
    }

}
