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

    default Product getOrLoad(Long productId, Callable<Product> loader) {
        return delegate().getOrLoad(
                key(productId),
                ttl(),
                new TypeReference<Product>() {},
                loader
        );
    }
}
