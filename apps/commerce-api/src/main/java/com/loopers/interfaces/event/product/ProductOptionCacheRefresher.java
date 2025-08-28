package com.loopers.interfaces.event.product;

import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionCache;
import com.loopers.domain.product.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductOptionCacheRefresher {
    private final ProductOptionCache productOptionCache;
    private final ProductOptionRepository productOptionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductChanged(ProductChangedEvent event) {
        Long productId = event.productId();
        try {
            productOptionCache.evict(productId);

            List<ProductOption> productOptions = productOptionRepository.findByProductId(productId);

            productOptionCache.put(productId, productOptions, productOptionCache.ttl());
            log.info("[ProductOptionCacheRefresher] refreshed cache for productId={}", productId);
        } catch (Exception e) {
            log.warn("[ProductOptionCacheRefresher] failed to refresh product option cache. productId={}", productId, e);
        }
    }
}
