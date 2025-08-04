package com.platform.common.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis JSON工具类（处理对象与Redis的JSON格式序列化/反序列化）
 */
@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
public class RedisJsonUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 将对象以JSON格式存储到Redis，并设置过期时间
     *
     * @param key     键
     * @param value   值（支持任意对象，null值会序列化为"null"）
     * @param timeout 过期时间（null表示永不过期）
     * @param unit    时间单位（timeout为null时忽略）
     */
    public void set(String key, Object value, Long timeout, TimeUnit unit) {
        String jsonValue = JSON.toJSONString(value); // 处理null：会序列化为"null"
        if (timeout != null && unit != null) {
            redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
        } else {
            redisTemplate.opsForValue().set(key, jsonValue);
        }
    }

    /**
     * 重载：无过期时间的set
     */
    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    /**
     * 获取指定key的JSON数据并反序列化为指定类型
     *
     * @param key   键
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象（key不存在或值为"null"时返回null）
     */
    public <T> T get(String key, Class<T> clazz) {
        String jsonValue = (String) redisTemplate.opsForValue().get(key);
        if (jsonValue == null || "null".equals(jsonValue)) { // 处理null值
            return null;
        }
        return JSON.parseObject(jsonValue, clazz);
    }

    /**
     * 获取字符串值的子串（仅用于纯字符串key，非JSON对象）
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @return 子字符串
     */
    public String get(String key, long start, long end) {
        return (String) redisTemplate.opsForValue().get(key, start, end);
    }

    /**
     * 设置新值并返回旧值（JSON格式）
     *
     * @param key   键
     * @param value 新值
     * @return 旧的JSON字符串值（可能为null）
     */
    public String getAndSet(String key, Object value) {
        String jsonValue = JSON.toJSONString(value);
        return (String) redisTemplate.opsForValue().getAndSet(key, jsonValue);
    }

    /**
     * 批量获取多个key的JSON数据并反序列化为指定类型列表
     *
     * @param keys  键集合
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象列表（顺序与keys一致，不存在的key对应null）
     */
    public <T> List<T> multiGet(Collection<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> jsonValues = redisTemplate.opsForValue().multiGet(keys);
        return jsonValues.stream()
                .map(json -> {
                    if (json == null || "null".equals(json)) {
                        return null;
                    }
                    return JSON.parseObject((String) json, clazz);
                })
                .collect(Collectors.toList());
    }

    /**
     * 仅当key不存在时设置值（JSON格式）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（null表示永不过期）
     * @param unit    时间单位（timeout为null时忽略）
     * @return key不存在并设置成功返回true，否则false
     */
    public boolean setIfAbsent(String key, Object value, Long timeout, TimeUnit unit) {
        String jsonValue = JSON.toJSONString(value);
        if (timeout != null && unit != null) {
            return redisTemplate.opsForValue().setIfAbsent(key, jsonValue, timeout, unit);
        } else {
            return redisTemplate.opsForValue().setIfAbsent(key, jsonValue);
        }
    }

    /**
     * 批量设置键值对（JSON格式）并统一设置过期时间
     *
     * @param maps    键值对映射（key为Redis键，value为任意对象）
     * @param timeout 过期时间（null表示永不过期）
     * @param unit    时间单位（timeout为null时忽略）
     */
    public void multiSet(Map<String, Object> maps, Long timeout, TimeUnit unit) {
        if (maps == null || maps.isEmpty()) {
            return;
        }
        // 逐个将value序列化为JSON字符串（避免整体转换导致的格式问题）
        Map<String, String> jsonMaps = maps.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> JSON.toJSONString(entry.getValue())
                ));
        redisTemplate.opsForValue().multiSet(jsonMaps);
        // 批量设置过期时间
        if (timeout != null && unit != null) {
            expire(maps.keySet(), timeout, unit);
        }
    }

    /**
     * 批量设置键值对（仅当所有key都不存在时）
     *
     * @param maps    键值对映射
     * @param timeout 过期时间（null表示永不过期）
     * @param unit    时间单位（timeout为null时忽略）
     * @return 所有key都不存在并设置成功返回true，否则false
     */
    public boolean multiSetIfAbsent(Map<String, Object> maps, Long timeout, TimeUnit unit) {
        if (maps == null || maps.isEmpty()) {
            return false;
        }
        Map<String, String> jsonMaps = maps.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> JSON.toJSONString(entry.getValue())
                ));
        Boolean result = redisTemplate.opsForValue().multiSetIfAbsent(jsonMaps);
        if (result != null && result && timeout != null && unit != null) {
            expire(maps.keySet(), timeout, unit);
        }
        return result != null && result;
    }

    /**
     * 为单个key设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间（>0）
     * @param unit    时间单位
     * @return 设置成功返回true，否则false
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (timeout <= 0) {
            return false;
        }
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 为多个key批量设置过期时间
     *
     * @param keys    键集合
     * @param timeout 过期时间（>0）
     * @param unit    时间单位
     */
    public void expire(Collection<String> keys, long timeout, TimeUnit unit) {
        if (keys == null || keys.isEmpty() || timeout <= 0) {
            return;
        }
        keys.forEach(key -> redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 删除单个key
     *
     * @param key 键
     * @return 删除成功返回true
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     *
     * @param keys 键集合
     * @return 成功删除的key数量
     */
    public Long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return 存在返回true
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 从列表左侧添加单个元素（JSON格式）
     *
     * @param key     键
     * @param value   元素值
     * @param timeout 过期时间（null表示不设置）
     * @param unit    时间单位（timeout为null时忽略）
     * @return 操作后列表的长度
     */
    public Long leftPush(String key, Object value, Long timeout, TimeUnit unit) {
        String jsonValue = JSON.toJSONString(value);
        Long size = redisTemplate.opsForList().leftPush(key, jsonValue);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 从列表右侧添加单个元素（JSON格式）
     *
     * @param key     键
     * @param value   元素值
     * @param timeout 过期时间（null表示不设置）
     * @param unit    时间单位（timeout为null时忽略）
     * @return 操作后列表的长度
     */
    public Long rightPush(String key, Object value, Long timeout, TimeUnit unit) {
        String jsonValue = JSON.toJSONString(value);
        Long size = redisTemplate.opsForList().rightPush(key, jsonValue);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 从列表左侧批量添加元素（JSON格式）
     *
     * @param key     键
     * @param values  元素集合（支持任意类型的集合，如List<ChatRobot>）
     * @param timeout 过期时间（null表示不设置）
     * @param unit    时间单位（timeout为null时忽略）
     * @return 操作后列表的长度
     */
    public Long leftPushAll(String key, Collection<?> values, Long timeout, TimeUnit unit) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        // 将任意类型的集合转为JSON字符串集合（泛型兼容）
        Collection<String> jsonValues = values.stream()
                .map(JSON::toJSONString)
                .collect(Collectors.toList());
        Long size = redisTemplate.opsForList().leftPushAll(key, jsonValues);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 从列表右侧批量添加元素（JSON格式）
     *
     * @param key     键
     * @param values  元素集合（支持任意类型的集合，如List<ChatRobot>）
     * @param timeout 过期时间（null表示不设置）
     * @param unit    时间单位（timeout为null时忽略）
     * @return 操作后列表的长度
     */
    public Long rightPushAll(String key, Collection<?> values, Long timeout, TimeUnit unit) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        Collection<String> jsonValues = values.stream()
                .map(JSON::toJSONString)
                .collect(Collectors.toList());
        Long size = redisTemplate.opsForList().rightPushAll(key, jsonValues);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 获取列表中指定范围的元素（反序列化为指定类型）
     *
     * @param key   键
     * @param start 起始索引（0表示第一个元素，-1表示最后一个）
     * @param end   结束索引
     * @param clazz 目标元素类型
     * @param <T>   泛型类型
     * @return 反序列化后的元素列表（空列表表示无数据）
     */
    public <T> List<T> range(String key, long start, long end, Class<T> clazz) {
        List<Object> jsonValues = redisTemplate.opsForList().range(key, start, end);
        if (jsonValues == null || jsonValues.isEmpty()) {
            return Collections.emptyList();
        }
        return jsonValues.stream()
                .map(json -> JSON.parseObject((String) json, clazz))
                .collect(Collectors.toList());
    }
}