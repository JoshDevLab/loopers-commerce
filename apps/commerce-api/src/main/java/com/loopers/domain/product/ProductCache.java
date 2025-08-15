package com.loopers.domain.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.infrastructure.cache.GenericCachePort;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface ProductCache {
    GenericCachePort delegate();
    Duration ttl();
    default String key(Long productId) {
        return "product:v1:" + productId;
    }

    default void put(Long productId, Product product, Duration ttl) {
        delegate().put(key(productId), product, ttl);
    }

    default Product getOrLoad(Long productId, Callable<Product> loader) {
        return delegate().getOrLoad(
                key(productId),
                ttl(),
                new TypeReference<Product>() {},
                loader
        );
    }

    default void evict(Long productId) {
        delegate().evict(key(productId));
    }
}
