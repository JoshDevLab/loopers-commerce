package com.loopers.domain.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.infrastructure.cache.GenericCachePort;
import org.springframework.data.domain.Page;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface ProductListCache {
    GenericCachePort delegate();
    Duration ttl();

    default String listKey(int page) {
        return "product:list:v1:page:" + page;
    }

    default Page<Product> getOrLoad(int page, Callable<Page<Product>> loader) {
        return delegate().getOrLoad(
                listKey(page),
                ttl(),
                new TypeReference<Page<Product>>() {},
                loader
        );
    }
}
