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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

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

    @Mock
    ProductListCache productListCache;

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

    @Test
    @DisplayName("기본 필터 + 1~3페이지 범위면 캐시를 사용하고, 캐시 히트 시 레포지토리를 호출하지 않는다")
    void usesCache_whenDefaultFilter_andPageInRange_cacheHit() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(true);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> cached = new PageImpl<>(List.of(mock(Product.class)), pageable, 100);

        when(productListCache.getOrLoad(eq(0), any())).thenReturn(cached);

        // Act
        Page<Product> result = sut.searchByConditionWithPaging(criteria, pageable);

        // Assert
        assertThat(result).isSameAs(cached);
        verify(productRepository, never()).findAllByCriteria(any(), any());
        verify(productListCache, times(0)).getOrLoad(eq(0), any());
    }

    @Test
    @DisplayName("기본 필터 + 1~3페이지 범위에서 캐시 미스면 로더로 DB 조회하여 반환하고, 그 결과가 캐시에 저장된다")
    void usesCache_whenDefaultFilter_andPageInRange_cacheMiss_thenLoadAndReturn() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(true);
        Pageable pageable = PageRequest.of(1, 20);
        Page<Product> loaded = new PageImpl<>(List.of(mock(Product.class)), pageable, 40);

        // 레포지토리 결과 설정
        when(productRepository.findAllByCriteria(criteria, pageable)).thenReturn(loaded);

        // 캐시 미스 → 첫 호출 시 loader.call() 실행, 이후엔 캐시 히트 시나리오 흉내
        AtomicReference<Page<Product>> stored = new AtomicReference<>();
        when(productListCache.getOrLoad(eq(1), any()))
                .thenAnswer(inv -> {
                    Callable<Page<Product>> loader = inv.getArgument(1);
                    if (stored.get() == null) {
                        Page<Product> firstLoad = loader.call(); // DB 조회
                        stored.set(firstLoad);                   // 캐시에 저장되었다고 가정
                        return firstLoad;
                    }
                    return stored.get(); // 두 번째부턴 캐시 히트
                });

        // Act
        Page<Product> first = sut.searchByConditionWithPaging(criteria, pageable);
        Page<Product> second = sut.searchByConditionWithPaging(criteria, pageable);

        // Assert
        assertThat(first).isSameAs(loaded);
        assertThat(second).isSameAs(loaded);

        verify(productRepository, times(1)).findAllByCriteria(criteria, pageable);

        InOrder inOrder = inOrder(productListCache, productRepository);
        inOrder.verify(productListCache).getOrLoad(eq(1), any());
        inOrder.verify(productRepository).findAllByCriteria(criteria, pageable);
        inOrder.verify(productListCache).getOrLoad(eq(1), any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("기본 필터가 아니면 캐시를 건너뛰고 바로 DB를 조회한다")
    void bypassCache_whenNotDefaultFilter() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(false);
        Pageable pageable = PageRequest.of(1, 20);
        Page<Product> db = new PageImpl<>(List.of(mock(Product.class)), pageable, 10);

        when(productRepository.findAllByCriteria(criteria, pageable)).thenReturn(db);

        // Act
        Page<Product> result = sut.searchByConditionWithPaging(criteria, pageable);

        // Assert
        assertThat(result).isSameAs(db);
        verify(productRepository, times(1)).findAllByCriteria(criteria, pageable);
        verifyNoInteractions(productListCache);
    }

    @Test
    @DisplayName("페이지가 캐싱 범위를 벗어나면(0페이지 또는 4페이지 이상) 캐시를 건너뛰고 DB를 조회한다")
    void bypassCache_whenPageOutOfRange() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(true);

        // page=0 (서비스 로직상 1~3만 캐시)
        Pageable p0 = PageRequest.of(0, 20);
        Page<Product> db0 = new PageImpl<>(List.of(mock(Product.class)), p0, 10);
        when(productRepository.findAllByCriteria(criteria, p0)).thenReturn(db0);

        // page=4
        Pageable p4 = PageRequest.of(3, 20);
        Page<Product> db4 = new PageImpl<>(List.of(mock(Product.class)), p4, 10);
        when(productRepository.findAllByCriteria(criteria, p4)).thenReturn(db4);

        // Act
        Page<Product> r0 = sut.searchByConditionWithPaging(criteria, p0);
        Page<Product> r4 = sut.searchByConditionWithPaging(criteria, p4);

        // Assert
        assertThat(r0).isSameAs(db0);
        assertThat(r4).isSameAs(db4);
        verify(productRepository, times(1)).findAllByCriteria(criteria, p0);
        verify(productRepository, times(1)).findAllByCriteria(criteria, p4);
        verifyNoInteractions(productListCache);
    }
}
