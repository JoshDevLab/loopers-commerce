package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.brand.dto.BrandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller {
    private final BrandFacade brandFacade;

    @GetMapping
    public ApiResponse<List<BrandResponse>> getBrandList() {
        List<BrandInfo> brandInfos = brandFacade.getBrands();
        return ApiResponse.success(
                brandInfos.stream()
                        .map(BrandResponse::from)
                        .toList()
        );
    }
}
