package com.loopers.infrastructure.productmetrics;

import com.loopers.domain.productmetrics.ProductMetrics;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")) // 5s
    Optional<ProductMetrics> findByProductIdAndMetricDate(Long productId, ZonedDateTime metricDate);
}
