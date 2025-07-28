package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.product.ProductCriteria;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.dto.ProductDetailResponse;
import com.loopers.interfaces.api.product.dto.ProductResponse;
import com.loopers.interfaces.api.product.dto.ProductSearchConditionRequest;
import com.loopers.interfaces.interceptor.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller {
    private final ProductFacade productFacade;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(ProductSearchConditionRequest condition, Pageable pageable) {
        Page<ProductInfo> productInfos = productFacade.getProductsWithCondition(ProductCriteria.toCriteria(condition), pageable);
        return ApiResponse.success(PageResponse.from(productInfos.map(ProductResponse::from)));
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long productId, @CurrentUser UserInfo userInfo) {
        ProductInfo productInfo = productFacade.getProductDetail(productId, userInfo);
        return ApiResponse.success(ProductDetailResponse.from(productInfo));
    }
}
