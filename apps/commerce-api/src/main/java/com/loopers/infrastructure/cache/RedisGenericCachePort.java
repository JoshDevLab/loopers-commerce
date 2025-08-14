package com.loopers.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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
        // 1) 캐시 조회: null 이거나 '빈 값'이면 미스로 처리
        T cached = get(key, typeRef);
        if (cached != null && !isEmptyValue(cached)) {
            return cached;
        }

        String lockKey = "lock:" + key;
        boolean acquired = Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS)
        );

        try {
            // 락을 못잡았으면 잠깐 대기 후 다시 확인
            if (!acquired) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                T again = get(key, typeRef);
                if (again != null && !isEmptyValue(again)) {
                    return again;
                }
            }

            // 로더 실행
            T loaded = loader.call();

            // 빈 결과는 '캐시하지 않음'
            if (loaded != null && !isEmptyValue(loaded)) {
                put(key, loaded, ttl);
            }

            return loaded;
        } catch (Exception e) {
            // CoreException만 그대로 통과, 나머지는 래핑
            if (e instanceof CoreException ce) throw ce;
            throw new RuntimeException("cache loader failed: " + e.getMessage(), e);
        } finally {
            if (acquired) redis.delete(lockKey);
        }
    }

    private boolean isEmptyValue(Object v) {
        switch (v) {
            case null -> {
                return true;
            }
            case CharSequence s -> {
                return s.isEmpty();
            }
            case Collection<?> c -> {
                return c.isEmpty();
            }
            case Map<?, ?> m -> {
                return m.isEmpty();
            }
            default -> {
            }
        }
        if (v.getClass().isArray()) return Array.getLength(v) == 0;
        if (v instanceof Optional<?> o) return o.isEmpty();
        return false;
    }
}
