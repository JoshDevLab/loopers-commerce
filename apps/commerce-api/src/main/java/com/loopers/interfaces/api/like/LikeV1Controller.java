package com.loopers.interfaces.api.like;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.dto.ProductResponse;
import com.loopers.interfaces.interceptor.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/like/products")
public class LikeV1Controller {
    private final ProductFacade productFacade;

    @GetMapping
    public ApiResponse<List<ProductResponse>> getLikedProducts(@CurrentUser UserInfo userInfo) {
        List<ProductInfo> productInfos = productFacade.getLikedProducts(userInfo.id());
        List<ProductResponse> result = productInfos
                .stream()
                .map(ProductResponse::from)
                .toList();
        return ApiResponse.success(result);
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Boolean> likeProduct(@PathVariable Long productId, @CurrentUser UserInfo userInfo) {
        return ApiResponse.success(productFacade.likeProduct(productId, userInfo.id()));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Boolean> unLikeProduct(@PathVariable Long productId, @CurrentUser UserInfo userInfo) {
        return ApiResponse.success(productFacade.unLikeProduct(productId, userInfo.id()));
    }
}
