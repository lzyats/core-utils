package com.platform.common.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * redis配置
 */
@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
@EnableCaching
public class RedisConfig {

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        return createTemplate(factory);
    }

    /**
     * 创建模板
     */
    public RedisTemplate<String, Object> createTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setDefaultSerializer(serializer);
        return redisTemplate;
    }

    @Value("${spring.other.host:}")
    private String host;

    @Value("${spring.other.port:6379}")
    private Integer port;

    @Value("${spring.other.database:0}")
    private Integer database;

    @Value("${spring.other.password:}")
    private String password;

    @Bean(name = "redisOtherTemplate")
    public RedisTemplate<String, Object> redisOtherTemplate(LettuceConnectionFactory factory) {
        if (!StringUtils.isEmpty(host)) {
            /* ========= 基本配置 ========= */
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setHostName(host);
            configuration.setPort(port);
            configuration.setDatabase(database);
            if (!StringUtils.isEmpty(password)) {
                configuration.setPassword(RedisPassword.of(password));
            }
            LettuceConnectionFactory newFactory = new LettuceConnectionFactory(configuration, factory.getClientConfiguration());
            newFactory.afterPropertiesSet();
            return createTemplate(newFactory);
        }
        return createTemplate(factory);
    }

    /**
     * 自定义RedisCacheManager，用于在使用@Cacheable时设置ttl
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getConnectionFactory());
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));
        return new TtlRedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }

    /**
     * 自定义RedisCacheManager
     */
    private class TtlRedisCacheManager extends RedisCacheManager {

        public TtlRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
            super(cacheWriter, defaultCacheConfiguration);
        }

        @Override
        protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
            return super.createRedisCache(name, cacheConfig.entryTtl(Duration.ofDays(30)));
        }
    }

}
