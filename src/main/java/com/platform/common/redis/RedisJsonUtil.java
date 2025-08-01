package com.platform.common.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis JSON工具类
 */
@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
public class RedisJsonUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 将对象以 JSON 格式存储到 Redis 中，并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        String jsonValue = JSON.toJSONString(value);
        redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
    }

    /**
     * 获取指定 key 的 JSON 数据并反序列化为指定类型的对象
     *
     * @param key   键
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象
     */
    public <T> T get(String key, Class<T> clazz) {
        String jsonValue = (String) redisTemplate.opsForValue().get(key);
        return JSON.parseObject(jsonValue, clazz);
    }

    /**
     * 返回 key 中 JSON 字符串值的子字符
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @return JSON 子字符串
     */
    public String get(String key, long start, long end) {
        return (String) redisTemplate.opsForValue().get(key, start, end);
    }

    /**
     * 将给定 key 的值设为 JSON 格式的 value ，并返回 key 的旧值(old value)
     *
     * @param key   键
     * @param value 值
     * @return 旧的 JSON 字符串值
     */
    public String getAndSet(String key, Object value) {
        String jsonValue = JSON.toJSONString(value);
        return (String) redisTemplate.opsForValue().getAndSet(key, jsonValue);
    }

    /**
     * 批量获取 JSON 数据并反序列化为指定类型的对象列表
     *
     * @param keys  键集合
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象列表
     */
    public <T> List<T> multiGet(Collection<String> keys, Class<T> clazz) {
        List<Object> jsonValues = redisTemplate.opsForValue().multiGet(keys);
        return JSON.parseArray(JSON.toJSONString(jsonValues), clazz);
    }

    /**
     * 只有在 key 不存在时设置 key 的 JSON 格式的值
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 之前已经存在返回 false，不存在返回 true
     */
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        String jsonValue = JSON.toJSONString(value);
        return redisTemplate.opsForValue().setIfAbsent(key, jsonValue, timeout, unit);
    }

    /**
     * 批量添加 JSON 格式的数据
     *
     * @param maps    键值对映射
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void multiSet(Map<String, Object> maps, long timeout, TimeUnit unit) {
        Map<String, String> jsonMaps = JSON.parseObject(JSON.toJSONString(maps), Map.class);
        redisTemplate.opsForValue().multiSet(jsonMaps);
        this.expire(maps.keySet(), timeout, unit);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     *
     * @param maps    键值对映射
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 之前已经存在返回 false，不存在返回 true
     */
    public boolean multiSetIfAbsent(Map<String, Object> maps, long timeout, TimeUnit unit) {
        Map<String, String> jsonMaps = JSON.parseObject(JSON.toJSONString(maps), Map.class);
        Boolean result = redisTemplate.opsForValue().multiSetIfAbsent(jsonMaps);
        this.expire(maps.keySet(), timeout, unit);
        return result;
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 设置过期时间
     *
     * @param keys    键集合
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void expire(Collection<String> keys, long timeout, TimeUnit unit) {
        for (String key : keys) {
            redisTemplate.expire(key, timeout, unit);
        }
    }

    /**
     * 删除 key
     *
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除 key
     *
     * @param keys 键集合
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 是否存在 key
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}