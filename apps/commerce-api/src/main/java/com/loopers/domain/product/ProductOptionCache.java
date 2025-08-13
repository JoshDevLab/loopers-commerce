package com.loopers.domain.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.infrastructure.cache.GenericCachePort;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

public interface ProductOptionCache {
    GenericCachePort delegate();
    Duration ttl();
    default String key(Long productId) {
        return "product:option:v1:" + productId;
    }

    default List<ProductOption> getOrLoad(
            Long productId,
            Callable<List<ProductOption>> loader
    ) {
        return delegate().getOrLoad(
                key(productId),
                ttl(),
                new TypeReference<List<ProductOption>>() {},
                loader
        );
    }
}
