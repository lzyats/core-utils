package com.platform.common.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis JSON工具类（处理对象与Redis的JSON格式序列化/反序列化，包含Hash类型操作）
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "spring.redis.enabled", havingValue = "Y")
public class RedisJsonUtilb {



    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    // 新增：从配置文件读取Redis数据库编号（避免硬编码，默认0号库）
    @Value("${spring.redis.database}")
    private int redisDbIndex;

    // ============================ 原有String/List类型方法（保持不变） ============================
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
     * 新增：支持泛型类型的反序列化（如List<CommonVo04>）
     * @param key 键
     * @param typeReference FastJSON的TypeReference，用于保留泛型信息
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        String jsonValue = (String) redisTemplate.opsForValue().get(key);
        if (jsonValue == null || "null".equals(jsonValue)) {
            return null;
        }
        return JSON.parseObject(jsonValue, typeReference);
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
        // 空key
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        // 不包含通配符
        if (!key.contains("*")) {
            return redisTemplate.delete(key);
        }
        // 包含通配符
        Set<String> keys = this.keys(key);
        if (!CollectionUtils.isEmpty(keys)) {
             delete(keys);
        }
        return true;
    }

    /**
     * 查找匹配的key
     *
     * @param pattern
     * @return
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 按通配符 pattern 删除缓存（优化版：确保扫描生效，带完整诊断日志）
     * @param pattern 通配符key（如 "a:b:*"，必须包含通配符才会批量匹配）
     * @return 成功删除的key数量
     */
    public Long deleteall(String pattern) {
        log.info("=== 开始执行缓存删除：pattern={} ===", pattern); // 移除数据库编号打印
        if (pattern == null || pattern.trim().isEmpty()) {
            log.info("缓存删除失败：通配符pattern不能为空");
            return 0L;
        }
        pattern = pattern.trim();

        List<String> matchedKeys = new ArrayList<>();
        RedisConnection connection = null;
        Cursor<byte[]> scanCursor = null;
        int scanRound = 0;

        try {
            // 1. 获取连接（不切换数据库，依赖连接工厂的默认配置）
            connection = getRedisConnection();
            if (connection == null || connection.isClosed()) {
                log.info("缓存删除失败：获取的Redis连接为null或已关闭");
                return 0L;
            }

            // 2. 构建Scan参数
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(2000)
                    .build();
            log.info("Scan参数：match={}, count={}", pattern, scanOptions.getCount());

            // 3. 执行单参数scan方法（适配Lettuce连接）
            scanCursor = connection.scan(scanOptions);

            // 4. 迭代所有匹配的key
            while (scanCursor.hasNext()) {
                scanRound++;
                byte[] keyBytes = scanCursor.next();
                String key = new String(keyBytes, StandardCharsets.UTF_8);
                matchedKeys.add(key);
                if (matchedKeys.size() % 1000 == 0) {
                    log.info("已扫描匹配key={}个", matchedKeys.size());
                }
            }

            // 5. 扫描完成诊断
            log.info("=== 扫描结束：共迭代{}次，累计匹配key={}个 ===", scanRound, matchedKeys.size());
            if (matchedKeys.isEmpty() && hasKey(pattern)) {
                log.info("发现单个key=[{}]，将尝试删除", pattern);
                matchedKeys.add(pattern);
            }

        } catch (Exception e) {
            log.info("扫描过程异常（pattern={}）", pattern, e);
            return 0L;
        } finally {
            // 6. 释放资源
            if (scanCursor != null) {
                try {
                    if (!scanCursor.isClosed()) {
                        scanCursor.close();
                        log.info("Scan游标已关闭");
                    }
                } catch (Exception e) {
                    log.info("关闭游标异常", e);
                }
            }
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                        log.info("Redis连接已关闭");
                    }
                } catch (Exception e) {
                    log.info("关闭连接异常", e);
                }
            }
        }

        // 7. 批量删除
        if (matchedKeys.isEmpty()) {
            log.info("无匹配key，删除数量=0");
            return 0L;
        }
        Long deletedCount = delete(matchedKeys);
        log.info("删除完成：匹配key={}个，成功删除={}个", matchedKeys.size(), deletedCount);
        return deletedCount;
    }

    /**
     * 获取Redis连接（移除数据库切换逻辑，依赖默认配置）
     */
    private RedisConnection getRedisConnection() {
        try {
            // 直接获取连接，不执行select（Lettuce共享连接不支持）
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            if (connection.isClosed()) {
                log.info("获取的Redis连接已关闭");
                return null;
            }
            log.info("获取Redis连接成功（使用默认配置的数据库）");
            return connection;
        } catch (Exception e) {
            log.info("获取Redis连接失败", e);
            return null;
        }
    }

    /**
     * 辅助方法：判断游标是否为"0"（Redis SCAN结束标志）
     */
    private boolean isCursorZero(byte[] cursor) {
        if (cursor == null) {
            return true; // 游标为null时视为结束
        }
        String cursorStr = new String(cursor, StandardCharsets.UTF_8);
        boolean isZero = "0".equals(cursorStr);
        log.debug("游标判断：cursorBytes={} → cursorStr={} → 是否结束={}",
                Arrays.toString(cursor), cursorStr, isZero);
        return isZero;
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
     * 将 ArrayList 中的每个元素作为独立元素，批量添加到 Redis LIST 的右侧（尾部）
     * （每个元素会被序列化为 JSON 字符串，最终 Redis LIST 的每个元素对应一个对象）
     *
     * @param key      Redis 键
     * @param list     要存储的 ArrayList（元素为任意对象，如 ArrayList<ChatUser>）
     * @param timeout  过期时间（null 表示永不过期）
     * @param unit     时间单位（timeout 为 null 时忽略）
     * @param <T>      ArrayList 中元素的类型
     * @return 操作后 Redis LIST 的总长度
     */
    public <T> Long pushArrayListToRedisList(String key, ArrayList<T> list, Long timeout, TimeUnit unit) {
        // 空集合直接返回 0，避免无效操作
        if (list == null || list.isEmpty()) {
            return 0L;
        }
        // 将 ArrayList 中的每个元素单独序列化为 JSON 字符串
        Collection<String> jsonElements = list.stream()
                .map(JSON::toJSONString) // 逐个元素序列化
                .collect(Collectors.toList());
        // 批量添加到 Redis LIST 的右侧（也可根据需求改为 leftPushAll 从左侧添加）
        Long totalSize = redisTemplate.opsForList().rightPushAll(key, jsonElements);
        // 设置过期时间（如指定）
        if (timeout != null && unit != null) {
            expire(key, timeout, unit);
        }
        return totalSize;
    }

    /**
     * 重载：无过期时间的版本
     */
    public <T> Long pushArrayListToRedisList(String key, ArrayList<T> list) {
        return pushArrayListToRedisList(key, list, null, null);
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

    // ============================ 新增Hash类型操作方法 ============================

    /**
     * 向Hash表中存入一个字段和值（值会序列化为JSON）
     *
     * @param key     Redis键（Hash表的key）
     * @param hashKey Hash表中的字段名
     * @param value   字段值（任意对象）
     * @param <T>     字段值的类型
     */
    public <T> void hset(String key, String hashKey, T value) {
        String jsonValue = JSON.toJSONString(value);
        redisTemplate.opsForHash().put(key, hashKey, jsonValue);
    }

    /**
     * 向Hash表中存入一个字段和值，并设置整个Hash表的过期时间
     *
     * @param key     Redis键（Hash表的key）
     * @param hashKey Hash表中的字段名
     * @param value   字段值（任意对象）
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     字段值的类型
     */
    public <T> void hset(String key, String hashKey, T value, long timeout, TimeUnit unit) {
        hset(key, hashKey, value);
        expire(key, timeout, unit);
    }

    /**
     * 从Hash表中获取指定字段的值（反序列化为指定类型）
     *
     * @param key     Redis键（Hash表的key）
     * @param hashKey Hash表中的字段名
     * @param clazz   目标类型
     * @param <T>     目标类型的泛型
     * @return 反序列化后的对象（字段不存在时返回null）
     */
    public <T> T hget(String key, String hashKey, Class<T> clazz) {
        String jsonValue = (String) redisTemplate.opsForHash().get(key, hashKey);
        if (jsonValue == null || "null".equals(jsonValue)) {
            return null;
        }
        return JSON.parseObject(jsonValue, clazz);
    }

    /**
     * 向Hash表中批量存入字段和值（值会序列化为JSON）
     *
     * @param key    Redis键（Hash表的key）
     * @param values 字段-值映射（key为hash字段名，value为任意对象）
     */
    public void hmset(String key, Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        // 将所有值序列化为JSON字符串
        Map<String, String> jsonValues = values.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> JSON.toJSONString(entry.getValue())
                ));
        redisTemplate.opsForHash().putAll(key, jsonValues);
    }

    /**
     * 向Hash表中批量存入字段和值，并设置整个Hash表的过期时间
     *
     * @param key     Redis键（Hash表的key）
     * @param values  字段-值映射
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void hmset(String key, Map<String, ?> values, long timeout, TimeUnit unit) {
        hmset(key, values);
        expire(key, timeout, unit);
    }

    /**
     * 从Hash表中批量获取指定字段的值（反序列化为指定类型）
     *
     * @param key      Redis键（Hash表的key）
     * @param hashKeys 要获取的字段名集合
     * @param clazz    目标类型
     * @param <T>      目标类型的泛型
     * @return 字段-值映射（顺序与hashKeys一致，不存在的字段对应null）
     */
    public <T> Map<String, T> hmget(String key, Collection<String> hashKeys, Class<T> clazz) {
        if (hashKeys == null || hashKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        // 转换为Object数组（RedisTemplate要求）
        List<Object> hashKeyList = new ArrayList<>(hashKeys);
        List<Object> jsonValues = redisTemplate.opsForHash().multiGet(key, hashKeyList);

        // 构建结果映射
        Map<String, T> result = new LinkedHashMap<>(hashKeys.size());
        Iterator<String> keyIter = hashKeys.iterator();
        Iterator<Object> valueIter = jsonValues.iterator();
        while (keyIter.hasNext() && valueIter.hasNext()) {
            String hashKey = keyIter.next();
            Object jsonValue = valueIter.next();
            if (jsonValue == null || "null".equals(jsonValue)) {
                result.put(hashKey, null);
            } else {
                result.put(hashKey, JSON.parseObject((String) jsonValue, clazz));
            }
        }
        return result;
    }

    /**
     * 获取Hash表中所有的字段和值（反序列化为指定类型）
     *
     * @param key   Redis键（Hash表的key）
     * @param clazz 目标值类型
     * @param <T>   目标类型的泛型
     * @return 所有字段-值的映射（空映射表示Hash表不存在或为空）
     */
    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }
        // 转换并反序列化
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(), // hash字段名
                        entry -> {
                            String jsonValue = (String) entry.getValue();
                            return jsonValue == null || "null".equals(jsonValue)
                                    ? null
                                    : JSON.parseObject(jsonValue, clazz);
                        },
                        (oldVal, newVal) -> newVal, // 解决key冲突（理论上不会发生）
                        LinkedHashMap::new // 保持插入顺序
                ));
    }

    /**
     * 获取Hash表中所有的字段和值，值反序列化为Map<String, Object>
     *
     * @param key Redis键（Hash表的key）
     * @return 所有字段-值的映射（key为Hash字段名，value为反序列化后的Map<String, Object>；空映射表示Hash表不存在或为空）
     */
    public Map<String, Map<String, Object>> hgetAllMap(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }

        // 转换并反序列化：每个value为JSON字符串，反序列化为Map<String, Object>
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(), // Hash字段名（转为String）
                        entry -> {
                            String jsonValue = (String) entry.getValue();
                            if (jsonValue == null || "null".equals(jsonValue)) {
                                return null;
                            }
                            try {
                                // 将JSON字符串反序列化为Map<String, Object>
                                return JSON.parseObject(jsonValue, new TypeReference<Map<String, Object>>() {});
                            } catch (JSONException e) {
                                return null; // 解析失败返回null，避免影响整体结果
                            }
                        },
                        (oldVal, newVal) -> newVal, // 解决key冲突（理论上不会发生）
                        LinkedHashMap::new // 保持插入顺序
                ));
    }

    /**
     * 删除Hash表中的一个或多个字段
     *
     * @param key       Redis键（Hash表的key）
     * @param hashKeys 要删除的字段名（可变参数）
     * @return 成功删除的字段数量
     */
    public Long hdel(String key, String... hashKeys) {
        if (hashKeys == null || hashKeys.length == 0) {
            return 0L;
        }
        // 转换为Object数组（RedisTemplate要求）
        Object[] keys = Arrays.stream(hashKeys).toArray();
        return redisTemplate.opsForHash().delete(key, keys);
    }

    /**
     * 判断Hash表中是否存在指定字段
     *
     * @param key     Redis键（Hash表的key）
     * @param hashKey 字段名
     * @return 存在返回true，否则false
     */
    public boolean hexists(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 获取Hash表中所有的字段名
     *
     * @param key Redis键（Hash表的key）
     * @return 字段名列表（空列表表示Hash表不存在或无字段）
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
     * 获取Hash表中所有字段的值（反序列化为指定类型）
     *
     * @param key   Redis键（Hash表的key）
     * @param clazz 目标值类型
     * @param <T>   目标类型的泛型
     * @return 值列表（空列表表示Hash表不存在或无字段）
     */
    public <T> List<T> hvals(String key, Class<T> clazz) {
        List<Object> jsonValues = redisTemplate.opsForHash().values(key);
        if (jsonValues == null || jsonValues.isEmpty()) {
            return Collections.emptyList();
        }
        return jsonValues.stream()
                .map(jsonVal -> {
                    String json = (String) jsonVal;
                    return json == null || "null".equals(json)
                            ? null
                            : JSON.parseObject(json, clazz);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取Hash表中字段的数量
     *
     * @param key Redis键（Hash表的key）
     * @return 字段数量（Hash表不存在时返回0）
     */
    public Long hlen(String key) {
        return redisTemplate.opsForHash().size(key);
    }
}
