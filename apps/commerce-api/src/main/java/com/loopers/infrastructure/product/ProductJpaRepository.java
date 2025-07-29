package com.loopers.infrastructure.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p JOIN FETCH p.brand WHERE p.id = :productId")
    Optional<Product> findWithBrandById(Long productId);

    @Query("SELECT p FROM Product p JOIN FETCH p.brand WHERE p.brand = :brand")
    List<Product> findByBrand(Brand brand);
}
