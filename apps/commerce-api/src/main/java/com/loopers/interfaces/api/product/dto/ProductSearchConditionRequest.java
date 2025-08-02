package com.loopers.interfaces.api.product.dto;

public record ProductSearchConditionRequest(
    String keyword,

    String categoryName,

    Long brandId,

    String sort
) {
}
