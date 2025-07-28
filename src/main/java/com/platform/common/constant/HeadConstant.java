package com.platform.common.constant;

/**
 * 头部常量
 */
public class HeadConstant {

    /**
     * 请求参数
     */
    public static final String REQUEST_PATH = "requestPath";

    /**
     * 请求参数
     */
    public static final String SIGN_PATH = "signPath";

    /**
     * 登录用户 redis key
     */
    public static final String TOKEN_REDIS_ADMIN = "token:admin:";

    /**
     * 登录用户 redis key
     */
    public static final String TOKEN_REDIS_APP = "token:app:";

    /**
     * 令牌
     */
    public static final String TOKEN_HEADER_ADMIN = "Authorization";

    /**
     * 令牌
     */
    public static final String TOKEN_HEADER_APP = "token";

    /**
     * 权限配置 Perm
     */
    public static final String PERM_ADMIN = "admin";

    /**
     * 权限配置 Perm
     */
    public static final String PERM_APP = "app";

    /**
     * 权限配置 Perm
     */
    public static final String PREFIX_APP = "/app/";

    /**
     * redis频道
     */
    public static final String REDIS_CHANNEL = "redis.";

    /**
     * redis频道
     */
    public static final String REDIS_CHANNEL_ADMIN = REDIS_CHANNEL + "admin";

    /**
     * base64图片前缀
     */
    public static final String BASE64_PREFIX = "data:image/png;base64,";

    /**
     * 版本号
     */
    public static final String VERSION = "version";

    /**
     * 签名
     */
    public static final String SIGN = "sign";

    /**
     * appId
     */
    public static final String APP_ID = "appId";

    /**
     * 签名
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * 设备
     */
    public static final String DEVICE = "device";

    /**
     * 设备android
     */
    public static final String DEVICE_ANDROID = DEVICE + "=android";

    /**
     * 设备ios
     */
    public static final String DEVICE_IOS = DEVICE + "=ios";

    /**
     * 设备windows
     */
    public static final String DEVICE_WINDOWS = DEVICE + "=windows";

}
