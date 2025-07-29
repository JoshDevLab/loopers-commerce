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

    @Transactional(readOnly = true)
    public Page<Product> searchByConditionWithPaging(ProductCriteria criteria, Pageable pageable) {
        return productRepository.findAllByCriteria(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductWithBrandById(Long productId) {
        return productRepository.findWithBrandById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_NOT_FOUND, "Product not found with id: " + productId));
    }

    public List<Product> getProductByBrand(Brand brand) {
        return productRepository.findByBrandId(brand);
    }
}
