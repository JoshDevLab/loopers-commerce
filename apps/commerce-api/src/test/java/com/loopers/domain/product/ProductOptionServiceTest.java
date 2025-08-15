package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductOptionServiceTest {

    @Mock
    ProductOptionRepository productOptionRepository;

    @Mock
    ProductOptionCache productOptionCache;

    @InjectMocks
    ProductOptionService sut;

    @Test
    @DisplayName("캐시 히트면 리포지토리를 호출하지 않는다")
    void getProductOptionsByProductId_cacheHit() {
        // Arrange
        Long productId = 1L;
        ProductOption option = mock(ProductOption.class);
        when(productOptionCache.getOrLoad(eq(productId), any()))
                .thenReturn(List.of(option));

        // Act
        List<ProductOption> result = sut.getProductOptionsByProductId(productId);

        // Assert
        assertThat(result).hasSize(1).contains(option);
        verifyNoInteractions(productOptionRepository);
    }

    @Test
    @DisplayName("캐시 미스 시 로더로 적재하고, 이후에는 캐시에서 가져온다")
    void getProductOptionsByProductId_cacheMiss_thenHit() throws Exception {
        // Arrange
        Long productId = 2L;
        ProductOption option = mock(ProductOption.class);
        AtomicReference<List<ProductOption>> stored = new AtomicReference<>();

        when(productOptionRepository.findByProductId(productId)).thenReturn(List.of(option));
        when(productOptionCache.getOrLoad(eq(productId), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Callable<List<ProductOption>> loader = invocation.getArgument(1, Callable.class);
                    if (stored.get() == null) { // 캐시 미스
                        List<ProductOption> loaded = loader.call(); // repo 호출
                        stored.set(loaded);
                        return loaded;
                    }
                    return stored.get(); // 캐시 히트
                });

        // Act
        List<ProductOption> first = sut.getProductOptionsByProductId(productId);
        List<ProductOption> second = sut.getProductOptionsByProductId(productId);

        // Assert
        assertThat(first).hasSize(1).contains(option);
        assertThat(second).hasSize(1).contains(option);
        verify(productOptionRepository, times(1)).findByProductId(productId);

        InOrder inOrder = inOrder(productOptionCache, productOptionRepository);
        inOrder.verify(productOptionCache).getOrLoad(eq(productId), any());
        inOrder.verify(productOptionRepository).findByProductId(productId);
        inOrder.verify(productOptionCache).getOrLoad(eq(productId), any());
        verifyNoMoreInteractions(productOptionRepository);
    }

    @Test
    @DisplayName("빈 리스트면 예외(Product option not found)를 던진다")
    void getProductOptionsByProductId_empty_throws() {
        // Arrange
        Long productId = 3L;
        when(productOptionCache.getOrLoad(eq(productId), any()))
                .thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> sut.getProductOptionsByProductId(productId))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다")
                .extracting("errorType")
                .isEqualTo(ErrorType.PRODUCT_OPTION_NOT_FOUND);
    }
}
