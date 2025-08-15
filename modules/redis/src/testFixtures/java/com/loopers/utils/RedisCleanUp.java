package com.loopers.utils;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCleanUp {

    private final StringRedisTemplate redisTemplate;

    public RedisCleanUp(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void clearAll() {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }
}
