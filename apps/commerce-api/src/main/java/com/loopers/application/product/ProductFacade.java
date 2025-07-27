package com.loopers.application.product;

import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;

    public Page<ProductInfo> getProductsWithCondition(ProductCriteria criteria, Pageable pageable) {
        return productService.searchByConditionWithPaging(criteria, pageable)
                .map(ProductInfo::from);
    }
}
