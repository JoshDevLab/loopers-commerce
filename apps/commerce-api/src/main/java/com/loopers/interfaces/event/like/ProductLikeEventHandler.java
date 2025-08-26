package com.loopers.interfaces.event.like;

import com.loopers.domain.product.like.ProductLikeEvent;
import com.loopers.domain.product.like.ProductLikeStatService;
import com.loopers.domain.product.like.ProductUnLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductLikeEventHandler {
    private final ProductLikeStatService productLikeStatService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductLike(ProductLikeEvent event) {
        productLikeStatService.like(event.productId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductUnLike(ProductUnLikeEvent event) {
        productLikeStatService.unLike(event.productId());
    }
}
