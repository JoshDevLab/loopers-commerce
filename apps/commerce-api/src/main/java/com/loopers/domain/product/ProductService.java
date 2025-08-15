package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCache productCache;
    private final ProductListCache productListCache;

    @Transactional(readOnly = true)
    public Page<Product> searchByConditionWithPaging(ProductCriteria criteria, Pageable pageable) {
        int page = pageable.getPageNumber();
        boolean isDefaultFilter = criteria.isDefault();
        boolean isCacheablePage = page >= 0 && page <= 2;

        if (isDefaultFilter && isCacheablePage) {
            return productListCache.getOrLoad(pageable, () -> productRepository.findAllByCriteria(criteria, pageable));
        }

        return productRepository.findAllByCriteria(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductWithBrandById(Long productId) {
        return productCache.getOrLoad(productId, () -> productRepository.findWithBrandById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_NOT_FOUND, "존재하지 않는 상품 id: " + productId)));
    }

    public List<Product> getProductByBrand(Brand brand) {
        return productRepository.findByBrandId(brand);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_NOT_FOUND, "존재하지 않는 상품 id: " + productId));
    }

    public Product getProductByIdWithLock(Long productId) {
        return productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_NOT_FOUND, "존재하지 않는 상품 id: " + productId));
    }
}
