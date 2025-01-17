package com.user_management_service.user_management_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void blacklistToken(String token, long timeToLiveMs) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", timeToLiveMs, TimeUnit.MILLISECONDS);
            log.debug("Token blacklisted successfully");
        } catch (Exception e) {
            log.error("Error blacklisting token", e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking token blacklist status", e);
            // Fail secure - if we can't check Redis, consider the token invalid
            return true;
        }
    }

    // Health check method
    public boolean isHealthy() {
        try {
            RedisCallback<Boolean> pingCallback = connection -> {
                try {
                    connection.ping();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            };
            Boolean result = redisTemplate.execute(pingCallback);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return false;
        }
    }
}