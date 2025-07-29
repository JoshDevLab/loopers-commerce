package com.loopers.application.brand;

import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;

    public List<BrandInfo> getBrands() {
        return brandService.getBrands().stream()
                .map(BrandInfo::from)
                .toList();
    }
}
