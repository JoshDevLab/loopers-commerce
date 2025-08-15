package com.loopers.domain.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.infrastructure.cache.GenericCachePort;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    default void evictAll() {
        for (int i = 0; i < 3; i++) {
            delegate().evict(listKey(i));
        }
    };

    default void loadAll() {
        for (int i = 0; i < 3; i++) {
            int page = i;
            delegate().getOrLoad(listKey(i), ttl(), new TypeReference<PageImpl<Product>>() {}, () -> {
                throw new CoreException(ErrorType.PRODUCT_LIST_CACHING_FAIL, "상품 목록 캐싱 실패: 페이지 " + page);
            });
        }
    };
}
