package com.platform.common.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;

import java.nio.charset.Charset;

/**
 * 短码工具类
 */
public class CodeUtils {

    /**
     * 基础密码
     */
    private static final String BASE_STR = "abcdefghgkmnprstwxyz123456789";

    /**
     * 密码
     */
    public static String password() {
        return password(8);
    }

    /**
     * 密码
     */
    public static String password(int length) {
        return RandomUtil.randomString(BASE_STR, length);
    }

    /**
     * 加密盐
     */
    public static String salt() {
        return RandomUtil.randomString(4);
    }

    /**
     * md5
     */
    public static final String md5(String str) {
        return SecureUtil.md5(str);
    }

    /**
     * credentials
     */
    public static final String credentials(String password, String salt) {
        byte[] digest = salt.getBytes(Charset.defaultCharset());
        Digester digester = DigestUtil.digester(DigestAlgorithm.MD5).setSalt(digest);
        return digester.digestHex(password);
    }

    /**
     * 验证码
     */
    public static String smsCode() {
        return smsCode(4);
    }

    /**
     * 验证码
     */
    public static String smsCode(Integer length) {
        return RandomUtil.randomString(RandomUtil.BASE_NUMBER, length);
    }

}
