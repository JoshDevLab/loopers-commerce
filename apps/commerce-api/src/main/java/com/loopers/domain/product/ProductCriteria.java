package com.loopers.domain.product;

import com.loopers.interfaces.api.product.dto.ProductSearchConditionRequest;

public record ProductCriteria(
    String keyword,
    ProductCategory category,
    Long brandId,
    String sort
) {

    public static ProductCriteria toCriteria(ProductSearchConditionRequest condition) {
        return new ProductCriteria(
            condition.keyword(),
            condition.categoryName() == null || condition.categoryName().isBlank() ?
                    null : ProductCategory.valueOfName(condition.categoryName()),
            condition.brandId(),
            condition.sort()
        );
    }

    public static ProductCriteria create(String keyword, ProductCategory category, Long brandId, String sortBy) {
        return new ProductCriteria(
            keyword,
            category,
            brandId,
            sortBy
        );
    }
}
