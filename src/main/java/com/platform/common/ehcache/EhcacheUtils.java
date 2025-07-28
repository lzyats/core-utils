package com.platform.common.ehcache;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "ehcache")
public class EhcacheUtils {

    @Resource
    private CacheManager ehcacheManager;

    /**
     * 获取Ehcache对象
     */
    public Cache getCache(String cacheName) {
        return ehcacheManager.getCache(cacheName);
    }

    /**
     * 获取Ehcache对象
     */
    public Cache getCache(String cacheName, Long second) {
        Cache cache = ehcacheManager.getCache(cacheName);
        if (cache == null) {
            CacheConfiguration cacheConfiguration = ehcacheManager.getConfiguration().getDefaultCacheConfiguration();
            cacheConfiguration.setName(cacheName);
            cacheConfiguration.setTimeToIdleSeconds(second);
            cacheConfiguration.setTimeToLiveSeconds(second);
            cache = new Cache(cacheConfiguration);
            ehcacheManager.addCache(cache);
        }
        return cache;
    }

    /**
     * 获取缓存
     */
    public String get(String cacheName, String key) {
        try {
            Cache cache = this.getCache(cacheName);
            if (cache == null) {
                return null;
            }
            Element element = cache.get(key);
            if (element == null) {
                return null;
            }
            return (String) element.getObjectValue();
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "get", cacheName, e.getMessage());
        }
        return null;
    }

    /**
     * 获取缓存
     */
    public List<String> getAll(String cacheName, List<String> keys) {
        // 结果集
        List<String> dataList = new ArrayList<>();
        // 集合判空
        if (CollectionUtils.isEmpty(keys)) {
            return dataList;
        }
        try {
            Cache cache = this.getCache(cacheName);
            if (cache == null) {
                return dataList;
            }
            cache.getAll(keys).forEach((key, element) -> {
                if (element != null) {
                    dataList.add((String) element.getObjectValue());
                }
            });
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "getAll", cacheName, e.getMessage());
        }
        return dataList;
    }

    /**
     * 获取所有key
     */
    public List<String> keys(String cacheName) {
        List<String> dataList = new ArrayList<>();
        try {
            Cache cache = this.getCache(cacheName);
            if (cache == null) {
                return dataList;
            }
            dataList = cache.getKeys();
            // 集合判空
            if (CollectionUtils.isEmpty(dataList)) {
                dataList = new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "keys", cacheName, e.getMessage());
        }
        return dataList;
    }

    /**
     * 保存缓存
     */
    public void put(String cacheName, Long second, String key, String value) {
        try {
            this.getCache(cacheName, second).put(new Element(key, value));
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "put", cacheName, e.getMessage());
        }
    }

    /**
     * 删除缓存
     */
    public void remove(String cacheName, String key) {
        try {
            Cache cache = this.getCache(cacheName);
            if (cache == null) {
                return;
            }
            cache.remove(key);
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "remove", cacheName, e.getMessage());
        }
    }

    /**
     * 删除缓存
     */
    public void removeAll(String cacheName, Collection<String> keys) {
        try {
            Cache cache = this.getCache(cacheName);
            if (cache == null) {
                return;
            }
            cache.removeAll(keys);
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "removeAll", cacheName, e.getMessage());
        }
    }

    /**
     * 替换缓存
     */
    public void replace(String cacheName, String key, String value) {
        try {
            Cache cache = this.getCache(cacheName);
            if (cache == null) {
                return;
            }
            cache.replace(new Element(key, value));
        } catch (Exception e) {
            log.error("方法:[{}],cacheName:[{}],异常:[{}]", "replace", cacheName, e.getMessage());
        }
    }

    /**
     * 关闭缓存
     */
    public void shutDownCache() {
        // 关闭缓存
        ehcacheManager.shutdown();
    }

}
