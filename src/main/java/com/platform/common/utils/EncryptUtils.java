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
     * 解密
     */
    public static String decrypt(String content, String secret) {
        // 构建aes
        AES aes = initAes(secret);
        // 解密为字符串
        return aes.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
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
