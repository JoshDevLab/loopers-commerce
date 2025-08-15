package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCache;
import com.loopers.domain.product.ProductChangedEvent;
import com.loopers.domain.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCacheRefresherTest {

    @Mock ProductCache productCache;
    @Mock ProductRepository productRepository;

    @InjectMocks ProductCacheRefresher sut;

    @Test
    @DisplayName("정상: evict 후 DB에서 다시 로드해서 put 한다")
    void refresh_ok() {
        // Arrange
        Long productId = 10L;
        Product p = mock(Product.class);
        when(productRepository.findWithBrandById(productId)).thenReturn(Optional.of(p));
        when(productCache.ttl()).thenReturn(Duration.ofMinutes(3));

        // Act
        sut.onProductChanged(new ProductChangedEvent(productId));

        // Assert
        InOrder inOrder = inOrder(productCache, productRepository, productCache);
        inOrder.verify(productCache).evict(productId);
        inOrder.verify(productRepository).findWithBrandById(productId);
        inOrder.verify(productCache).put(productId, p, Duration.ofMinutes(3));
        verifyNoMoreInteractions(productCache, productRepository);
    }

    @Test
    @DisplayName("예외: DB에 없으면 put하지 않고 경고만 (evict는 수행)")
    void refresh_notFound() {
        // Arrange
        Long productId = 20L;
        when(productRepository.findWithBrandById(productId)).thenReturn(Optional.empty());

        // Act
        sut.onProductChanged(new ProductChangedEvent(productId));

        // Assert
        verify(productCache, times(1)).evict(productId);
        verify(productRepository, times(1)).findWithBrandById(productId);
        verify(productCache, never()).put(any(), any(), any());
    }

    @Test
    @DisplayName("예외: put에서 에러가 나도 전체 흐름은 잡아먹지 않는다(경고 로그)")
    void refresh_putThrows_but_swallowed() {
        // Arrange
        Long productId = 30L;
        Product p = mock(Product.class);
        when(productRepository.findWithBrandById(productId)).thenReturn(Optional.of(p));
        when(productCache.ttl()).thenReturn(Duration.ofMinutes(3));
        doThrow(new RuntimeException("redis down")).when(productCache).put(eq(productId), eq(p), any());

        // Act
        sut.onProductChanged(new ProductChangedEvent(productId));

        // Assert
        verify(productCache).evict(productId);
        verify(productRepository).findWithBrandById(productId);
        verify(productCache).put(eq(productId), eq(p), any());
        // 예외를 밖으로 던지지 않음
    }
}
