package com.loopers.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
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

@Slf4j
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
            log.warn("[cache:get] deserialize failed. key={}, will return null (treat as miss). cause={}", key, e.toString());
            return null;
        }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(key, json, ttl.toMillis(), TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            log.warn("[cache:put] serialize failed. key={}, valueType={}, cause={}", key,
                    value != null ? value.getClass().getName() : "null", e.toString());
        }
    }

    @Override
    public void evict(String key) {
        redis.delete(key);
    }

    @Override
    public <T> T getOrLoad(String key, Duration ttl, TypeReference<T> typeRef, Callable<T> loader) {
        T cached = safeReadOrEvictOnCorruption(key, typeRef);
        if (cached != null && !isEmptyValue(cached)) {
            return cached;
        }

        String lockKey = "lock:" + key;
        boolean acquired = Boolean.TRUE.equals(
                redis.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS)
        );

        try {
            if (!acquired) {
                // 잠깐 대기 후 재확인
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                T again = safeReadOrEvictOnCorruption(key, typeRef);
                if (again != null && !isEmptyValue(again)) {
                    return again;
                }
            }

            // 로더(DB) 실행
            T loaded = loader.call();

            // 빈 결과는 캐시하지 않음
            if (loaded != null && !isEmptyValue(loaded)) {
                put(key, loaded, ttl);
            }
            return loaded;

        } catch (Exception e) {
            // CoreException은 그대로 전달
            if (e instanceof CoreException ce) throw ce;
            log.warn("[cache:getOrLoad] loader failed. key={}, cause={}", key, e.toString());
            throw new RuntimeException("cache loader failed: " + e.getMessage(), e);

        } finally {
            if (acquired) redis.delete(lockKey);
        }
    }

    private <T> T safeReadOrEvictOnCorruption(String key, TypeReference<T> typeRef) {
        String json = redis.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (IOException ex) {
            log.warn("[cache:corruption] deserialize failed. key={} -> evict & treat as miss. cause={}", key, ex.toString());
            evict(key);
            return null;
        }
    }

    private boolean isEmptyValue(Object v) {
        switch (v) {
            case null -> { return true; }
            case CharSequence s -> { return s.isEmpty(); }
            case Collection<?> c -> { return c.isEmpty(); }
            case Map<?, ?> m -> { return m.isEmpty(); }
            case Page<?> p -> { return p.isEmpty(); }
            case Slice<?> s -> { return !s.hasContent(); }
            default -> { /* fall-through */ }
        }
        if (v.getClass().isArray()) return Array.getLength(v) == 0;
        if (v instanceof Optional<?> o) return o.isEmpty();
        if (v instanceof Iterable<?> it) return !it.iterator().hasNext();
        return false;
    }
}
