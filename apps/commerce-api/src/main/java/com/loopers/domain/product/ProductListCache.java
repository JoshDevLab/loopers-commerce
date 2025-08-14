package com.loopers.domain.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.infrastructure.cache.GenericCachePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface ProductListCache {
    GenericCachePort delegate();
    Duration ttl();

    default String listKey(int page) {
        return "product:list:v1:page:" + page;
    }

    default Page<Product> getOrLoad(Pageable pageable, Callable<Page<Product>> loader) {
        return delegate().getOrLoad(
                listKey(pageable.getPageNumber()),
                ttl(),
                new TypeReference<PageImpl<Product>>() {},
                () -> {
                    Page<Product> p = loader.call();
                    return new PageImpl<>(p.getContent(), pageable, p.getTotalElements());
                }
        );
    }
}
