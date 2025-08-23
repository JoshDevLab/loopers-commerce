package com.loopers.application.product;

import com.loopers.domain.product.ProductListAddedEvent;
import com.loopers.domain.product.ProductListCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductListCacheRefresher {
    private final ProductListCache productListCache;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void addedProduct(ProductListAddedEvent event) {
        try {
            productListCache.evictAll();
            productListCache.loadAll();
            log.info("[ProductListCacheRefresher] refreshed product list cache");
        } catch (Exception e) {
            log.warn("[ProductListCacheRefresher] failed to refresh product list cache", e);
        }
    }
}
