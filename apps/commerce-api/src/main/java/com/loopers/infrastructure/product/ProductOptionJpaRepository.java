package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductOption;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductOption p where p.id = :productOptionId")
    Optional<ProductOption> findByIdWithLock(Long productOptionId);

    @Query("select po from ProductOption po join fetch po.product where po.id in(:optionIds)")
    List<ProductOption> findByIdInWithFetch(List<Long> optionIds);
}
