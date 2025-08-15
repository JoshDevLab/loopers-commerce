package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCache;
import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    private final ProductRepository productRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void onProductChanged(ProductChangedEvent event) {
        Long productId = event.productId();
        try {
            productCache.evict(productId);

            Product p = productRepository.findWithBrandById(productId)
                    .orElseThrow(() -> new CoreException(
                            ErrorType.PRODUCT_NOT_FOUND, "존재하지 않는 상품입니다. id=" + productId));

            productCache.put(productId, p, productCache.ttl());
            log.info("[ProductCacheRefresher] refreshed cache for productId={}", productId);
        } catch (Exception e) {
            log.warn("[ProductCacheRefresher] failed to refresh product cache. productId={}", productId, e);
        }
    }
}
