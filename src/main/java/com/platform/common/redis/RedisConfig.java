package com.platform.common.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import org.springframework.data.redis.serializer.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * redis配置：同时定义字符串序列化和JSON序列化的Template
 */
@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
@EnableCaching
public class RedisConfig {

    // ============================ 1. 字符串序列化Template（供RedisUtils使用） ============================
    /**
     * 字符串专用Template：Key和Value均用String序列化（适合纯字符串操作）
     */
    @Bean
    @Primary // 若有其他地方默认注入RedisTemplate，优先使用此JSON模板（可根据需求调整）
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        return createTemplate(factory);
    }

    // ============================ 2. JSON序列化Template（供RedisJsonUtil使用） ============================
    /**
     * JSON专用Template：Key用String序列化，Value用FastJSON序列化（适合对象操作）
     */
    @Bean(name = "jsonRedisTemplate")
    public RedisTemplate<String, Object> jsonRedisTemplate(LettuceConnectionFactory factory) {
        return createJsonTemplate(factory);
    }

    /**
     * 创建模板
     */
    public RedisTemplate<String, Object> createTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        // 1. Key/HashKey用String序列化（无引号）
        StringRedisSerializer stringSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setDefaultSerializer(serializer);
        return redisTemplate;
    }

    /**
     * 创建JSON序列化的Template（复用逻辑，支持多实例）
     */
    private RedisTemplate<String, Object> createJsonTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. Key/HashKey用String序列化（无引号）
        StringRedisSerializer stringSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 2. Value/HashValue用FastJSON序列化（对象转JSON）
        FastJsonRedisSerializer<Object> fastJsonSerializer = new FastJsonRedisSerializer<>(Object.class);
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        template.afterPropertiesSet(); // 初始化配置
        return template;
    }

    // ============================ 3. 保留多Redis实例配置（可选，适配备用Redis） ============================
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
     * 备用Redis实例：JSON序列化模板
     */
    @Bean(name = "otherJsonRedisTemplate")
    public RedisTemplate<String, Object> otherJsonRedisTemplate(LettuceConnectionFactory factory) {
        if (!StringUtils.isEmpty(host)) {
            LettuceConnectionFactory newFactory = createOtherConnectionFactory(factory);
            return createJsonTemplate(newFactory); // 复用JSON序列化配置
        }
        return createJsonTemplate(factory);
    }

    /**
     * 创建备用Redis连接工厂
     */
    private LettuceConnectionFactory createOtherConnectionFactory(LettuceConnectionFactory defaultFactory) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);
        if (!StringUtils.isEmpty(password)) {
            config.setPassword(RedisPassword.of(password));
        }
        // 复用默认客户端配置（如超时时间、连接池等）
        LettuceConnectionFactory newFactory = new LettuceConnectionFactory(config, defaultFactory.getClientConfiguration());
        newFactory.afterPropertiesSet();
        return newFactory;
    }

    // ============================ 4. 缓存管理器配置（保持不变，按需调整） ============================
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