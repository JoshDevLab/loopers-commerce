package com.loopers.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisGenericCachePort implements GenericCachePort {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public <T> T get(String key, TypeReference<T> typeRef) {
        String json = redis.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(key, json, ttl.toMillis(), TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void evict(String key) {
        redis.delete(key);
    }

    @Override
    public <T> T getOrLoad(String key, Duration ttl, TypeReference<T> typeRef, Callable<T> loader) {
        T cached = get(key, typeRef);
        if (cached != null) return cached;

        String lockKey = "lock:" + key;
        boolean acquired = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS));

        try {
            if (!acquired) {
                Thread.sleep(50);
                T again = get(key, typeRef);
                if (again != null) return again;
            }
            T loaded = loader.call();
            if (loaded != null) put(key, loaded, ttl);
            return loaded;
        } catch (Exception e) {
            throw new CoreException(((CoreException) e).getErrorType(), e.getMessage());
        } finally {
            if (acquired) redis.delete(lockKey);
        }
    }
}
