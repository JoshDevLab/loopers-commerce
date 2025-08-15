package com.loopers.domain.product;

import com.loopers.infrastructure.cache.GenericCachePort;
import com.loopers.infrastructure.cache.RedisGenericCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class ProductPotionCacheImpl implements ProductOptionCache {
    private final RedisGenericCachePort delegate;

    @Override
    public GenericCachePort delegate() {
        return delegate;
    }

    @Override
    public Duration ttl() {
        return Duration.ofMinutes(5);
    }
}
