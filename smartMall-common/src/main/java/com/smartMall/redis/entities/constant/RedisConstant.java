package com.smartMall.redis.entities.constant;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2026/1/24 21:11
 */
public class RedisConstant {

    public static final String REDIS_KEY_PREFIX = "smartMall:";

    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkCode:";

    public static final long REDIS_KEY_CHECK_CODE_EXPIRE_TIME = 60 * 5;

    public static final long REDIS_KEY_TOKEN_ONE_MINUTE_EXPIRE_TIME = 60;

    public static final long REDIS_KEY_TOKEN_ONE_HOUR_EXPIRE_TIME = 60 * 60;

    public static final long REDIS_KEY_TOKEN_ONE_DAY_EXPIRE_TIME = 60 * 60 * 24;

    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX + "token:admin:";
}
