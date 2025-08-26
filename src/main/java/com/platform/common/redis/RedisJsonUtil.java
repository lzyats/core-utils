package com.platform.common.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis JSON 工具类（处理对象与 Redis 的 JSON 格式序列化/反序列化，包含 Hash 类型操作）
 * 已适配：Key 无引号（StringRedisSerializer）、Value 用 FastJSON 序列化
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
public class RedisJsonUtil {

    // 注入自定义配置的 RedisTemplate（Key无引号，Value用FastJSON）
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 从配置文件读取 Redis 数据库编号（默认 0 号库）
    @Value("${spring.redis.database:0}")
    private int redisDbIndex;

    // ============================ String/List 类型操作 ============================
    /**
     * 将对象存储到 Redis（FastJSON 自动序列化），并设置过期时间
     * @param key     键（无引号）
     * @param value   值（任意对象，null 时存储为 null）
     * @param timeout 过期时间（null 表示永不过期）
     * @param unit    时间单位（timeout 为 null 时忽略）
     */
    public void set(String key, Object value, Long timeout, TimeUnit unit) {
        if (timeout != null && unit != null) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 重载：无过期时间的 set
     */
    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    /**
     * 获取指定 key 的值并反序列化为指定类型
     * @param key   键（无引号）
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象（key 不存在时返回 null）
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // 强转为目标类型（FastJSON 已保证类型匹配）
        return clazz.cast(value);
    }

    /**
     * 支持泛型类型的反序列化（如 List<CommonVo>、Map<String, User>）
     * @param key           键（无引号）
     * @param typeReference FastJSON TypeReference（保留泛型信息）
     * @param <T>           泛型类型
     * @return 反序列化后的对象
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // 泛型需先转 JSON 字符串再解析（避免类型擦除）
        String jsonValue = JSON.toJSONString(value);
        return JSON.parseObject(jsonValue, typeReference);
    }

    /**
     * 获取字符串值的子串（仅用于纯字符串 key）
     * @param key   键（无引号）
     * @param start 起始位置
     * @param end   结束位置
     * @return 子字符串
     */
    public String get(String key, long start, long end) {
        Object value = redisTemplate.opsForValue().get(key, start, end);
        return value == null ? null : value.toString();
    }

    /**
     * 设置新值并返回旧值（FastJSON 自动序列化/反序列化）
     * @param key   键（无引号）
     * @param value 新值
     * @return 旧值（JSON 字符串格式）
     */
    public String getAndSet(String key, Object value) {
        Object oldValue = redisTemplate.opsForValue().getAndSet(key, value);
        return oldValue == null ? null : JSON.toJSONString(oldValue);
    }

    /**
     * 批量获取多个 key 的值并反序列化为指定类型列表
     * @param keys  键集合（无引号）
     * @param clazz 目标对象类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象列表（顺序与 keys 一致，不存在的 key 对应 null）
     */
    public <T> List<T> multiGet(Collection<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        return values.stream()
                .map(value -> value == null ? null : clazz.cast(value))
                .collect(Collectors.toList());
    }

    /**
     * 仅当 key 不存在时设置值（FastJSON 自动序列化）
     * @param key     键（无引号）
     * @param value   值
     * @param timeout 过期时间（null 表示永不过期）
     * @param unit    时间单位（timeout 为 null 时忽略）
     * @return key 不存在并设置成功返回 true，否则 false
     */
    public boolean setIfAbsent(String key, Object value, Long timeout, TimeUnit unit) {
        if (timeout != null && unit != null) {
            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        } else {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        }
    }

    /**
     * 批量设置键值对（FastJSON 自动序列化）并统一设置过期时间
     * @param maps    键值对映射（key 为 Redis 键，value 为任意对象）
     * @param timeout 过期时间（null 表示永不过期）
     * @param unit    时间单位（timeout 为 null 时忽略）
     */
    public void multiSet(Map<String, Object> maps, Long timeout, TimeUnit unit) {
        if (maps == null || maps.isEmpty()) {
            return;
        }
        redisTemplate.opsForValue().multiSet(maps);
        // 批量设置过期时间
        if (timeout != null && unit != null) {
            expire(maps.keySet(), timeout, unit);
        }
    }

    /**
     * 批量设置键值对（仅当所有 key 都不存在时）
     * @param maps    键值对映射
     * @param timeout 过期时间（null 表示永不过期）
     * @param unit    时间单位（timeout 为 null 时忽略）
     * @return 所有 key 都不存在并设置成功返回 true，否则 false
     */
    public boolean multiSetIfAbsent(Map<String, Object> maps, Long timeout, TimeUnit unit) {
        if (maps == null || maps.isEmpty()) {
            return false;
        }
        Boolean result = redisTemplate.opsForValue().multiSetIfAbsent(maps);
        if (result != null && result && timeout != null && unit != null) {
            expire(maps.keySet(), timeout, unit);
        }
        return result != null && result;
    }

    /**
     * 为单个 key 设置过期时间
     * @param key     键（无引号）
     * @param timeout 过期时间（>0）
     * @param unit    时间单位
     * @return 设置成功返回 true，否则 false
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (timeout <= 0 || StringUtils.isEmpty(key)) {
            return false;
        }
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 为多个 key 批量设置过期时间
     * @param keys    键集合（无引号）
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
     * 删除单个 key（支持通配符）
     * @param key 键（无引号，支持 * 通配符）
     * @return 删除成功返回 true
     */
    public Boolean delete(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        // 无通配符：直接删除
        if (!key.contains("*")) {
            return redisTemplate.delete(key);
        }
        // 有通配符：批量匹配删除
        Set<String> keys = this.keys(key);
        if (!CollectionUtils.isEmpty(keys)) {
            delete(keys);
        }
        return true;
    }

    /**
     * 按通配符批量删除缓存（优化版：使用 SCAN 避免 KEYS 阻塞）
     * @param pattern 通配符 key（如 "user:*"，必须包含 * 才会批量匹配）
     * @return 成功删除的 key 数量
     */
    public Long deleteall(String pattern) {
        log.info("=== 开始执行缓存删除：pattern={} ===", pattern);
        if (pattern == null || pattern.trim().isEmpty()) {
            log.info("缓存删除失败：通配符 pattern 不能为空");
            return 0L;
        }
        pattern = pattern.trim();

        List<String> matchedKeys = new ArrayList<>();
        RedisConnection connection = null;
        Cursor<byte[]> scanCursor = null;
        int scanRound = 0;

        try {
            // 获取 Redis 连接（使用默认数据库配置）
            connection = getRedisConnection();
            if (connection == null || connection.isClosed()) {
                log.info("缓存删除失败：获取的 Redis 连接为 null 或已关闭");
                return 0L;
            }

            // 构建 SCAN 参数（count=2000 平衡性能与效率）
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(2000)
                    .build();
            log.info("SCAN 参数：match={}, count={}", pattern, scanOptions.getCount());

            // 执行 SCAN 扫描匹配的 key
            scanCursor = connection.scan(scanOptions);
            while (scanCursor.hasNext()) {
                scanRound++;
                byte[] keyBytes = scanCursor.next();
                String key = new String(keyBytes, StandardCharsets.UTF_8);
                matchedKeys.add(key);
                if (matchedKeys.size() % 1000 == 0) {
                    log.info("已扫描匹配 key={} 个", matchedKeys.size());
                }
            }

            // 扫描结果诊断（处理单个 key 误判）
            log.info("=== 扫描结束：共迭代{}次，累计匹配 key={}个 ===", scanRound, matchedKeys.size());
            if (matchedKeys.isEmpty() && hasKey(pattern)) {
                log.info("发现单个 key=[{}]，将尝试删除", pattern);
                matchedKeys.add(pattern);
            }

        } catch (Exception e) {
            log.error("SCAN 扫描过程异常（pattern={}）", pattern, e);
            return 0L;
        } finally {
            // 释放资源（游标 + 连接）
            if (scanCursor != null) {
                try {
                    if (!scanCursor.isClosed()) {
                        scanCursor.close();
                        log.info("SCAN 游标已关闭");
                    }
                } catch (Exception e) {
                    log.error("关闭 SCAN 游标异常", e);
                }
            }
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                        log.info("Redis 连接已关闭");
                    }
                } catch (Exception e) {
                    log.error("关闭 Redis 连接异常", e);
                }
            }
        }

        // 批量删除匹配的 key
        if (matchedKeys.isEmpty()) {
            log.info("无匹配 key，删除数量=0");
            return 0L;
        }
        Long deletedCount = delete(matchedKeys);
        log.info("删除完成：匹配 key={}个，成功删除={}个", matchedKeys.size(), deletedCount);
        return deletedCount;
    }

    /**
     * 获取 Redis 连接（使用默认数据库配置）
     */
    private RedisConnection getRedisConnection() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection.isClosed()) {
                log.warn("获取的 Redis 连接已关闭，尝试重新获取");
                connection = redisTemplate.getConnectionFactory().getConnection();
            }
            log.info("获取 Redis 连接成功（使用默认数据库：{}）", redisDbIndex);
            return connection;
        } catch (Exception e) {
            log.error("获取 Redis 连接失败", e);
            return null;
        }
    }

    /**
     * 查找匹配的 key（支持通配符）
     * @param pattern 通配符（如 "user:*"）
     * @return 匹配的 key 集合（无引号）
     */
    public Set<String> keys(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return Collections.emptySet();
        }
        return redisTemplate.keys(pattern);
    }

    /**
     * 批量删除 key
     * @param keys 键集合（无引号）
     * @return 成功删除的 key 数量
     */
    public Long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 判断 key 是否存在
     * @param key 键（无引号）
     * @return 存在返回 true，否则 false
     */
    public Boolean hasKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return redisTemplate.hasKey(key);
    }

    // ============================ List 类型操作 ============================
    /**
     * 从列表左侧添加单个元素（FastJSON 自动序列化）
     * @param key     键（无引号）
     * @param value   元素值（任意对象）
     * @param timeout 过期时间（null 表示不设置）
     * @param unit    时间单位（timeout 为 null 时忽略）
     * @return 操作后列表的长度
     */
    public Long leftPush(String key, Object value, Long timeout, TimeUnit unit) {
        Long size = redisTemplate.opsForList().leftPush(key, value);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 从列表右侧添加单个元素（FastJSON 自动序列化）
     * @param key     键（无引号）
     * @param value   元素值（任意对象）
     * @param timeout 过期时间（null 表示不设置）
     * @param unit    时间单位（timeout 为 null 时忽略）
     * @return 操作后列表的长度
     */
    public Long rightPush(String key, Object value, Long timeout, TimeUnit unit) {
        Long size = redisTemplate.opsForList().rightPush(key, value);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 从列表左侧批量添加元素（FastJSON 自动序列化）
     * @param key     键（无引号）
     * @param values  元素集合（任意对象）
     * @param timeout 过期时间（null 表示不设置）
     * @param unit    时间单位（timeout 为 null 时忽略）
     * @return 操作后列表的长度
     */
    public Long leftPushAll(String key, Collection<?> values, Long timeout, TimeUnit unit) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        Long size = redisTemplate.opsForList().leftPushAll(key, values);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 从列表右侧批量添加元素（FastJSON 自动序列化）
     * @param key     键（无引号）
     * @param values  元素集合（任意对象）
     * @param timeout 过期时间（null 表示不设置）
     * @param unit    时间单位（timeout 为 null 时忽略）
     * @return 操作后列表的长度
     */
    public Long rightPushAll(String key, Collection<?> values, Long timeout, TimeUnit unit) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        Long size = redisTemplate.opsForList().rightPushAll(key, values);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return size;
    }

    /**
     * 将 ArrayList 元素批量添加到 Redis LIST 右侧（每个元素自动序列化）
     * @param key      键（无引号）
     * @param list     待存储的 ArrayList（元素为任意对象）
     * @param timeout  过期时间（null 表示永不过期）
     * @param unit     时间单位（timeout 为 null 时忽略）
     * @param <T>      ArrayList 元素类型
     * @return 操作后 Redis LIST 的总长度
     */
    public <T> Long pushArrayListToRedisList(String key, ArrayList<T> list, Long timeout, TimeUnit unit) {
        if (list == null || list.isEmpty()) {
            return 0L;
        }
        Long totalSize = redisTemplate.opsForList().rightPushAll(key, list);
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return totalSize;
    }

    /**
     * 重载：无过期时间的 ArrayList 批量添加
     */
    public <T> Long pushArrayListToRedisList(String key, ArrayList<T> list) {
        return pushArrayListToRedisList(key, list, null, null);
    }

    /**
     * 获取列表中指定范围的元素并反序列化为指定类型
     * @param key   键（无引号）
     * @param start 起始索引（0 表示第一个元素，-1 表示最后一个）
     * @param end   结束索引
     * @param clazz 目标元素类型
     * @param <T>   泛型类型
     * @return 反序列化后的元素列表（空列表表示无数据）
     */
    public <T> List<T> range(String key, long start, long end, Class<T> clazz) {
        List<Object> values = redisTemplate.opsForList().range(key, start, end);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(value -> clazz.cast(value))
                .collect(Collectors.toList());
    }

    // ============================ Hash 类型操作 ============================
    /**
     * 向 Hash 表中存入一个字段和值（FastJSON 自动序列化）
     * @param key     Redis 键（Hash 表的 key，无引号）
     * @param hashKey Hash 表中的字段名
     * @param value   字段值（任意对象）
     * @param <T>     字段值类型
     */
    public <T> void hset(String key, String hashKey, T value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 向 Hash 表中存入字段和值，并设置 Hash 表过期时间
     * @param key     Redis 键（Hash 表的 key，无引号）
     * @param hashKey Hash 表中的字段名
     * @param value   字段值（任意对象）
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     字段值类型
     */
    public <T> void hset(String key, String hashKey, T value, long timeout, TimeUnit unit) {
        hset(key, hashKey, value);
        expire(key, timeout, unit);
    }

    /**
     * 从 Hash 表中获取指定字段的值并反序列化为指定类型
     * @param key     Redis 键（Hash 表的 key，无引号）
     * @param hashKey Hash 表中的字段名
     * @param clazz   目标类型
     * @param <T>     目标类型泛型
     * @return 反序列化后的对象（字段不存在时返回 null）
     */
    public <T> T hget(String key, String hashKey, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, hashKey);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 向 Hash 表中批量存入字段和值（FastJSON 自动序列化）
     * @param key    Redis 键（Hash 表的 key，无引号）
     * @param values 字段-值映射（key 为 hash 字段名，value 为任意对象）
     */
    public void hmset(String key, Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, values);
    }

    /**
     * 向 Hash 表批量存入字段和值，并设置 Hash 表过期时间
     * @param key     Redis 键（Hash 表的 key，无引号）
     * @param values  字段-值映射
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void hmset(String key, Map<String, ?> values, long timeout, TimeUnit unit) {
        hmset(key, values);
        expire(key, timeout, unit);
    }

    /**
     * 从 Hash 表中批量获取指定字段的值并反序列化为指定类型
     * @param key      Redis 键（Hash 表的 key，无引号）
     * @param hashKeys 要获取的字段名集合
     * @param clazz    目标类型
     * @param <T>      目标类型泛型
     * @return 字段-值映射（顺序与 hashKeys 一致，不存在的字段对应 null）
     */
    public <T> Map<String, T> hmget(String key, Collection<String> hashKeys, Class<T> clazz) {
        if (hashKeys == null || hashKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object> hashKeyList = new ArrayList<>(hashKeys);
        List<Object> values = redisTemplate.opsForHash().multiGet(key, hashKeyList);

        // 构建结果映射
        Map<String, T> result = new LinkedHashMap<>(hashKeys.size());
        Iterator<String> keyIter = hashKeys.iterator();
        Iterator<Object> valueIter = values.iterator();
        while (keyIter.hasNext() && valueIter.hasNext()) {
            String hashKey = keyIter.next();
            Object value = valueIter.next();
            result.put(hashKey, value == null ? null : clazz.cast(value));
        }
        return result;
    }

    /**
     * 获取 Hash 表中所有字段和值，并反序列化为指定类型
     * @param key   Redis 键（Hash 表的 key，无引号）
     * @param clazz 目标值类型
     * @param <T>   目标类型泛型
     * @return 所有字段-值映射（空映射表示 Hash 表不存在或为空）
     */
    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }
        // 转换并反序列化（保持插入顺序）
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        entry -> entry.getValue() == null ? null : clazz.cast(entry.getValue()),
                        (oldVal, newVal) -> newVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * 获取 Hash 表中所有字段和值，值反序列化为 Map<String, Object>
     * @param key Redis 键（Hash 表的 key，无引号）
     * @return 字段-值映射（value 为 Map<String, Object>）
     */
    public Map<String, Map<String, Object>> hgetAllMap(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }

        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        entry -> {
                            if (entry.getValue() == null) {
                                return null;
                            }
                            // 泛型 Map 需手动解析
                            String jsonValue = JSON.toJSONString(entry.getValue());
                            try {
                                return JSON.parseObject(jsonValue, new TypeReference<Map<String, Object>>() {});
                            } catch (JSONException e) {
                                log.error("Hash 值反序列化为 Map 失败，JSON：{}", jsonValue, e);
                                return null;
                            }
                        },
                        (oldVal, newVal) -> newVal,
                        LinkedHashMap::new
                ));
    }

    /**
     * 删除 Hash 表中的一个或多个字段
     * @param key      Redis 键（Hash 表的 key，无引号）
     * @param hashKeys 要删除的字段名（可变参数）
     * @return 成功删除的字段数量
     */
    public Long hdel(String key, String... hashKeys) {
        if (hashKeys == null || hashKeys.length == 0) {
            return 0L;
        }
        Object[] keys = Arrays.stream(hashKeys).toArray();
        return redisTemplate.opsForHash().delete(key, keys);
    }

    /**
     * 判断 Hash 表中是否存在指定字段
     * @param key     Redis 键（Hash 表的 key，无引号）
     * @param hashKey 字段名
     * @return 存在返回 true，否则 false
     */
    public boolean hexists(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 获取 Hash 表中所有字段名
     * @param key Redis 键（Hash 表的 key，无引号）
     * @return 字段名列表（空列表表示 Hash 表不存在或无字段）
     */
    public List<String> hkeys(String key) {
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return keys.stream()
                .map(keyObj -> (String) keyObj)
                .collect(Collectors.toList());
    }

    /**
     * 获取 Hash 表中所有字段的值并反序列化为指定类型
     * @param key   Redis 键（Hash 表的 key，无引号）
     * @param clazz 目标值类型
     * @param <T>   目标类型泛型
     * @return 值列表（空列表表示 Hash 表不存在或无字段）
     */
    public <T> List<T> hvals(String key, Class<T> clazz) {
        List<Object> values = redisTemplate.opsForHash().values(key);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(value -> value == null ? null : clazz.cast(value))
                .collect(Collectors.toList());
    }

    /**
     * 获取 Hash 表中字段的数量
     * @param key Redis 键（Hash 表的 key，无引号）
     * @return 字段数量（Hash 表不存在时返回 0）
     */
    public Long hlen(String key) {
        return redisTemplate.opsForHash().size(key);
    }
}