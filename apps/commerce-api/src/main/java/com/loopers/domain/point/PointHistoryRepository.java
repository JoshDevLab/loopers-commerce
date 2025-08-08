package com.loopers.domain.point;

import java.util.Optional;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);

    boolean existsByUserId(Long userPk);

    Optional<PointHistory> findByOrderId(Long orderId);

    void delete(PointHistory pointHistory);
}
