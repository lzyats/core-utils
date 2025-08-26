package com.platform.common.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 基于 Alibaba FastJSON 的 Redis 序列化器
 * 处理 Object 与 JSON 字节数组的转换，支持泛型与多态反序列化
 * @param <T> 序列化的目标对象类型
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    // 默认字符集（固定 UTF-8，避免平台编码差异）
    // 修正：类型从 StandardCharsets 改为 Charset
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 目标对象的 Class 类型（反序列化时需要）
    private final Class<T> targetClass;

    static {
        // 1. 开启 AutoType 支持（反序列化多态类型/泛型类型时必须，如 List<User>、BaseDTO 子类）
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);

        // 2. 【生产环境强制配置】AutoType 白名单（仅允许信任的包下类，防止反序列化漏洞）
        // 示例：允许 com.platform 下所有子包的类（根据项目实际包路径调整）
        ParserConfig.getGlobalInstance().addAccept("com.platform.");
        // 若有其他外部依赖包的类需反序列化，需追加白名单，如：
        // ParserConfig.getGlobalInstance().addAccept("com.alibaba.fastjson.");
    }

    /**
     * 构造器：指定目标对象类型
     * @param targetClass 反序列化的目标 Class
     */
    public FastJsonRedisSerializer(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * 序列化：Object → JSON 字符串 → byte[]
     * @param t 待序列化的对象（null 时返回空字节数组）
     */
    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        // FastJSON 序列化配置：解决常见问题
        return JSON.toJSONString(
                t,
                SerializerFeature.DisableCircularReferenceDetect, // 禁用循环引用（避免生成 $ref 占位符）
                SerializerFeature.WriteDateUseDateFormat,          // 日期统一格式：yyyy-MM-dd HH:mm:ss
                SerializerFeature.WriteClassName                  // 写入类名（支持多态反序列化，如子类对象）
                //SerializerFeature.IgnoreNonFieldGetter             // 忽略无 getter 的字段（避免序列化无效字段）
        ).getBytes(DEFAULT_CHARSET);
    }

    /**
     * 反序列化：byte[] → JSON 字符串 → Object
     * @param bytes 待反序列化的字节数组（null/空数组时返回 null）
     */
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String jsonStr = new String(bytes, DEFAULT_CHARSET);
        try {
            // 反序列化为目标类型
            return JSON.parseObject(jsonStr, targetClass);
        } catch (Exception e) {
            throw new SerializationException("FastJSON 反序列化失败，JSON 字符串：" + jsonStr, e);
        }
    }
}