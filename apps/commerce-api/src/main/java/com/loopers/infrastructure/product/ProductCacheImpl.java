package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCache;
import com.loopers.infrastructure.cache.GenericCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class ProductCacheImpl implements ProductCache {
    private final GenericCachePort delegate;

    @Override
    public GenericCachePort delegate() {
        return delegate;
    }

    @Override
    public Duration ttl() {
        return Duration.ofMinutes(5);
    }

}
