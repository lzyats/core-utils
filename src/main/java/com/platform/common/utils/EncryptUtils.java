package com.platform.common.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.AES;

/**
 * 消息加密
 */
public class EncryptUtils {

    /**
     * 加密
     */
    public static String encrypt(String content, String secret) {
        // 构建aes
        AES aes = initAes(secret);
        // 加密为16进制表示
        return aes.encryptHex(content);
    }

    /**
     * 加密对象
     */
    public static String encrypt(Object data, String secret) {
        // 构建aes
        AES aes = initAes(secret);
        // 1. 对象序列化为JSON字符串
        String jsonData = com.alibaba.fastjson.JSON.toJSONString(data);
        // 加密为16进制表示
        return aes.encryptHex(jsonData);
    }

    /**
     * 解密
     */
    public static String decrypt(String content, String secret) {
        // 构建aes
        AES aes = initAes(secret);
        // 解密为字符串
        return aes.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
    }

    /**
     * 解密：Base64解码→AES解密→JSON字符串→反序列化为对象
     */
    public static <T> T decrypt(String encryptedData, Class<T> clazz,String secret) throws Exception {
        if (encryptedData == null) {
            return null;
        }
        // 构建aes
        AES aes = initAes(secret);
        String jsonData = aes.decryptStr(encryptedData, CharsetUtil.CHARSET_UTF_8);
        // 3. JSON反序列化为对象
        return com.alibaba.fastjson.JSON.parseObject(jsonData, clazz);
    }

    /**
     * 构建aes
     */
    private static AES initAes(String secret) {
        return new AES("CBC", "PKCS7Padding", secret.getBytes(), secret.getBytes());
    }

    /**
     * 生成加密秘钥
     */
    public static String generate() {
        return com.baomidou.mybatisplus.core.toolkit.AES.generateRandomKey();
    }

}
