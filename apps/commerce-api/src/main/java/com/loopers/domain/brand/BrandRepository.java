package com.loopers.domain.brand;

import java.util.List;

public interface BrandRepository {
    Brand save(Brand brand);

    List<Brand> findAll();
}
