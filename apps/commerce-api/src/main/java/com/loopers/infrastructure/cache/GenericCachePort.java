package com.loopers.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface GenericCachePort {
    <T> T get(String key, TypeReference<T> typeRef);

    <T> void put(String key, T value, Duration ttl);

    void evict(String key);

    <T> T getOrLoad(String key, Duration ttl, TypeReference<T> typeRef, Callable<T> loader);}
