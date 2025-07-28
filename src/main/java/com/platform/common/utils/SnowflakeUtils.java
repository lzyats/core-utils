package com.platform.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

/**
 * 雪花id
 */
public class SnowflakeUtils {

    private Snowflake snowflake;

    private SnowflakeUtils() {
        Long workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr()) >> 16 & 31;
        this.snowflake = IdUtil.getSnowflake(workerId, 1L);
    }

    private static class SingletonHolder {
        private static final SnowflakeUtils singleton = new SnowflakeUtils();
    }

    public static SnowflakeUtils instance() {
        return SnowflakeUtils.SingletonHolder.singleton;
    }

    public Long getSnowflakeNextId() {
        return snowflake.nextId();
    }

    public static Long getNextId() {
        return SnowflakeUtils.instance().getSnowflakeNextId();
    }

}