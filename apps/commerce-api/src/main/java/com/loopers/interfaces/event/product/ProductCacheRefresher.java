package com.loopers.interfaces.event.product;

import com.loopers.domain.product.ProductCache;
import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.product.ProductRepository;
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
public class ProductCacheRefresher {
    private final ProductCache productCache;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductChanged(ProductChangedEvent event) {
        Long productId = event.productId();
        try {
            productCache.evict(productId);
        } catch (Exception e) {
            log.warn("[ProductCacheRefresher] failed to refresh product cache. productId={}", productId, e);
        }
    }
}
