package com.loopers.application.product;

import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.product.ProductOption;
import com.loopers.domain.product.ProductOptionCache;
import com.loopers.domain.product.ProductOptionRepository;
import com.loopers.interfaces.event.product.ProductOptionCacheRefresher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductOptionCacheRefresherTest {

    @Mock
    ProductOptionCache productOptionCache;

    @Mock
    ProductOptionRepository productOptionRepository;

    @InjectMocks
    ProductOptionCacheRefresher sut;

    @Nested
    @DisplayName("onProductChanged")
    class OnProductChanged {

        @Test
        @DisplayName("정상: evict → repo 조회 → put 순으로 캐시 갱신한다")
        void refresh_ok() {
            // Arrange
            Long productId = 10L;
            var event = new ProductChangedEvent(productId);

            ProductOption opt1 = mock(ProductOption.class);
            ProductOption opt2 = mock(ProductOption.class);
            List<ProductOption> loaded = List.of(opt1, opt2);
            Duration ttl = Duration.ofMinutes(3);

            when(productOptionRepository.findByProductId(productId)).thenReturn(loaded);
            when(productOptionCache.ttl()).thenReturn(ttl);

            // Act & Assert (예외 발생 없이 수행)
            assertThatCode(() -> sut.onProductChanged(event)).doesNotThrowAnyException();

            // Verify: 호출 순서와 파라미터
            InOrder inOrder = inOrder(productOptionCache, productOptionRepository);
            inOrder.verify(productOptionCache).evict(productId);
            inOrder.verify(productOptionRepository).findByProductId(productId);
            inOrder.verify(productOptionCache).put(eq(productId), eq(loaded), eq(ttl));
            verifyNoMoreInteractions(productOptionRepository, productOptionCache);
        }

        @Test
        @DisplayName("빈 리스트여도 evict 후 빈 리스트를 TTL과 함께 캐싱한다")
        void refresh_withEmptyList() {
            // Arrange
            Long productId = 20L;
            var event = new ProductChangedEvent(productId);

            List<ProductOption> loaded = List.of(); // 빈 리스트
            Duration ttl = Duration.ofMinutes(3);

            when(productOptionRepository.findByProductId(productId)).thenReturn(loaded);
            when(productOptionCache.ttl()).thenReturn(ttl);

            // Act & Assert
            assertThatCode(() -> sut.onProductChanged(event)).doesNotThrowAnyException();

            // Verify
            InOrder inOrder = inOrder(productOptionCache, productOptionRepository);
            inOrder.verify(productOptionCache).evict(productId);
            inOrder.verify(productOptionRepository).findByProductId(productId);
            inOrder.verify(productOptionCache).put(eq(productId), eq(loaded), eq(ttl));
            verifyNoMoreInteractions(productOptionRepository, productOptionCache);
        }

        @Test
        @DisplayName("repo 예외 발생 시 put은 호출하지 않고, 예외는 전파하지 않는다")
        void refresh_repoThrows_noPut_andNoThrow() {
            // Arrange
            Long productId = 30L;
            var event = new ProductChangedEvent(productId);

            when(productOptionRepository.findByProductId(productId))
                    .thenThrow(new RuntimeException("DB error"));

            // Act & Assert: 내부 try-catch로 예외 전파 안 됨
            assertThatCode(() -> sut.onProductChanged(event)).doesNotThrowAnyException();

            // Verify: evict는 호출, put은 호출 안 됨
            verify(productOptionCache, times(1)).evict(productId);
            verify(productOptionRepository, times(1)).findByProductId(productId);
            verify(productOptionCache, never()).put(anyLong(), anyList(), any());
            verifyNoMoreInteractions(productOptionRepository, productOptionCache);
        }
    }
}
