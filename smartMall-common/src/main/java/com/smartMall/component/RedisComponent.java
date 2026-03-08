package com.smartMall.component;

import com.smartMall.entities.constant.RedisConstant;
import com.smartMall.utils.RedisUtils;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 21:03
 */
@Component
public class RedisComponent {

    @Resource
    private RedisUtils<String> redisUtils;

    public String saveCheckCode(String code) {
        String uuid = UUID.randomUUID().toString();
        String checkCodeKey = RedisConstant.REDIS_KEY_CHECK_CODE + uuid;
        redisUtils.setex(checkCodeKey, code, RedisConstant.REDIS_KEY_CHECK_CODE_EXPIRE_TIME);
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(normalizeCheckCodeKey(checkCodeKey));
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(normalizeCheckCodeKey(checkCodeKey));
    }

    public String saveToken(String account) {
        String token = UUID.randomUUID().toString();
        String tokenKey = RedisConstant.REDIS_KEY_TOKEN_ADMIN + token;
        redisUtils.setex(tokenKey, account, RedisConstant.REDIS_KEY_TOKEN_ONE_DAY_EXPIRE_TIME);
        return token;
    }

    public String getToken(String tokenKey) {
        return (String) redisUtils.get(RedisConstant.REDIS_KEY_TOKEN_ADMIN + tokenKey);
    }

    public void refreshToken(String adminToken, String account) {
        if (StringTools.isEmpty(adminToken) || StringTools.isEmpty(account)) {
            return;
        }
        redisUtils.setex(RedisConstant.REDIS_KEY_TOKEN_ADMIN + adminToken,
                account, RedisConstant.REDIS_KEY_TOKEN_ONE_DAY_EXPIRE_TIME);
    }

    public void cleanToken(String adminToken) {
        redisUtils.delete(RedisConstant.REDIS_KEY_TOKEN_ADMIN + adminToken);
    }

    public String saveUserToken(String userId) {
        String token = UUID.randomUUID().toString();
        String tokenKey = RedisConstant.REDIS_KEY_TOKEN_USER + token;
        redisUtils.setex(tokenKey, userId, RedisConstant.REDIS_KEY_TOKEN_ONE_DAY_EXPIRE_TIME);
        return token;
    }

    public String getUserToken(String userToken) {
        return (String) redisUtils.get(RedisConstant.REDIS_KEY_TOKEN_USER + userToken);
    }

    public void cleanUserToken(String userToken) {
        redisUtils.delete(RedisConstant.REDIS_KEY_TOKEN_USER + userToken);
    }

    private String normalizeCheckCodeKey(String checkCodeKey) {
        if (StringTools.isEmpty(checkCodeKey)) {
            return checkCodeKey;
        }
        if (checkCodeKey.startsWith(RedisConstant.REDIS_KEY_CHECK_CODE)) {
            return checkCodeKey;
        }
        return RedisConstant.REDIS_KEY_CHECK_CODE + checkCodeKey;
    }
}
