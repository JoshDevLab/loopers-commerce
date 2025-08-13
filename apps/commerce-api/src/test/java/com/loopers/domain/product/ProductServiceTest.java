package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductCache productCache;

    @InjectMocks
    ProductService sut;

    @Test
    @DisplayName("캐시 히트 시, Repository는 호출되지 않는다.")
    void getProductWithBrandById_whenCacheHit_thenNoRepositoryCall() {
        // Arrange
        Long id = 100L;
        Product cached = mock(Product.class);
        when(productCache.getOrLoad(eq(id), any())).thenReturn(cached);

        // Act
        Product result = sut.getProductWithBrandById(id);

        // Assert
        assertThat(result).isSameAs(cached);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("캐시 미스 시, Repository에서 조회하여 반환한다.")
    void getProductWithBrandById_whenCacheMiss_thenLoadFromRepository() throws Exception {
        // Arrange
        Long id = 200L;
        Product loaded = mock(Product.class);
        when(productRepository.findWithBrandById(id)).thenReturn(Optional.of(loaded));
        when(productCache.getOrLoad(eq(id), any()))
                .thenAnswer(inv -> {
                    @SuppressWarnings("unchecked")
                    Callable<Product> loader = inv.getArgument(1);
                    return loader.call();
                });

        // Act
        Product result = sut.getProductWithBrandById(id);

        // Assert
        assertThat(result).isSameAs(loaded);
        verify(productRepository, times(1)).findWithBrandById(id);
    }

    @Test
    @DisplayName("캐시 미스 + Repository 조회 결과 없음 시, 예외를 던진다.")
    void getProductWithBrandById_whenCacheMissAndNotFound_thenThrowException() {
        // Arrange
        Long id = 300L;
        when(productRepository.findWithBrandById(id)).thenReturn(Optional.empty());
        when(productCache.getOrLoad(eq(id), any()))
                .thenAnswer(inv -> {
                    @SuppressWarnings("unchecked")
                    Callable<Product> loader = inv.getArgument(1);
                    try {
                        return loader.call();
                    } catch (Exception e) {
                        throw new CoreException(ErrorType.PRODUCT_NOT_FOUND, e.getMessage());
                    }
                });

        // Act & Assert
        assertThatThrownBy(() -> sut.getProductWithBrandById(id))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("존재하지 않는 상품 id")
                .extracting("errorType")
                .isEqualTo(ErrorType.PRODUCT_NOT_FOUND);

        verify(productRepository, times(1)).findWithBrandById(id);
    }
}
