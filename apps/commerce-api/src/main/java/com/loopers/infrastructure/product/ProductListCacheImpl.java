package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductListCache;
import com.loopers.infrastructure.cache.GenericCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class ProductListCacheImpl implements ProductListCache {
    private final GenericCachePort delegate;

    @Override
    public GenericCachePort delegate() {
        return delegate;
    }

    @Override
    public Duration ttl() {
        return Duration.ofMinutes(10);
    }
}
