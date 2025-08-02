package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;
    private final ProductService productService;

    public List<BrandInfo> getBrands() {
        return brandService.getBrands().stream()
                .map(BrandInfo::from)
                .toList();
    }

    public BrandInfo getBrandByIdWithProducts(Long id) {
        Brand brand = brandService.getBrand(id);
        List<Product> products = productService.getProductByBrand(brand);
        return BrandInfo.from(brand, products);
    }
}
