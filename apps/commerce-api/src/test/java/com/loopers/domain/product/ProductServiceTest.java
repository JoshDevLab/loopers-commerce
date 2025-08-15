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

    @Mock ProductRepository productRepository;
    @Mock ProductCache productCache;
    @Mock ProductListCache productListCache;

    @InjectMocks ProductService sut;

    @Test
    @DisplayName("캐시 히트면 Repository를 호출하지 않는다")
    void getProductWithBrandById_cacheHit() {
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
    @DisplayName("캐시 미스면 Repository에서 조회해 반환한다")
    void getProductWithBrandById_cacheMiss_thenLoadFromRepository() {
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
    @DisplayName("캐시 미스 + Repository 결과 없음이면 예외를 던진다")
    void getProductWithBrandById_cacheMiss_andNotFound_throws() {
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

    @Test
    @DisplayName("기본 필터 + 캐싱 범위(0~2페이지) && 캐시 히트면 Repository를 호출하지 않는다")
    void searchByCondition_cacheHit_inRange_defaultFilter() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(true);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> cached = new PageImpl<>(List.of(mock(Product.class)), pageable, 100);

        when(productListCache.getOrLoad(eq(pageable), any())).thenReturn(cached);

        // Act
        Page<Product> result = sut.searchByConditionWithPaging(criteria, pageable);

        // Assert
        assertThat(result).isSameAs(cached);
        verify(productListCache, times(1)).getOrLoad(eq(pageable), any());
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("기본 필터 + 캐싱 범위(0~2페이지) && 캐시 미스면 DB 조회 후 다음엔 캐시 히트가 된다")
    void searchByCondition_cacheMiss_thenHit_inRange_defaultFilter() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(true);

        Pageable pageable = PageRequest.of(1, 20);
        Page<Product> loaded = new PageImpl<>(List.of(mock(Product.class)), pageable, 40);

        when(productRepository.findAllByCriteria(criteria, pageable)).thenReturn(loaded);

        AtomicReference<Page<Product>> stored = new AtomicReference<>();
        when(productListCache.getOrLoad(eq(pageable), any()))
                .thenAnswer(inv -> {
                    @SuppressWarnings("unchecked")
                    Callable<Page<Product>> loader = inv.getArgument(1);
                    if (stored.get() == null) {
                        Page<Product> first = loader.call(); // DB hit
                        stored.set(first);                   // 캐시에 저장되었다고 가정
                        return first;
                    }
                    return stored.get();                   // cache hit
                });

        // Act
        Page<Product> first = sut.searchByConditionWithPaging(criteria, pageable);
        Page<Product> second = sut.searchByConditionWithPaging(criteria, pageable);

        // Assert
        assertThat(first).isSameAs(loaded);
        assertThat(second).isSameAs(loaded);
        verify(productRepository, times(1)).findAllByCriteria(criteria, pageable);

        InOrder inOrder = inOrder(productListCache, productRepository);
        inOrder.verify(productListCache).getOrLoad(eq(pageable), any());
        inOrder.verify(productRepository).findAllByCriteria(criteria, pageable);
        inOrder.verify(productListCache).getOrLoad(eq(pageable), any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("기본 필터가 아니면 캐시를 사용하지 않고 DB를 조회한다")
    void searchByCondition_notDefaultFilter_bypassCache() {
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
    @DisplayName("페이지가 캐시 범위를 벗어나면(3페이지 이상) 캐시를 건너뛰고 DB를 조회한다")
    void searchByCondition_pageOutOfRange_bypassCache() {
        // Arrange
        ProductCriteria criteria = mock(ProductCriteria.class);
        when(criteria.isDefault()).thenReturn(true);

        // page=3 (0-based, 즉 4페이지) → 범위 밖
        Pageable page3 = PageRequest.of(3, 20);
        Page<Product> db3 = new PageImpl<>(List.of(mock(Product.class)), page3, 10);
        when(productRepository.findAllByCriteria(criteria, page3)).thenReturn(db3);

        // Act
        Page<Product> result = sut.searchByConditionWithPaging(criteria, page3);

        // Assert
        assertThat(result).isSameAs(db3);
        verify(productRepository, times(1)).findAllByCriteria(criteria, page3);
        verifyNoInteractions(productListCache);
    }
}
