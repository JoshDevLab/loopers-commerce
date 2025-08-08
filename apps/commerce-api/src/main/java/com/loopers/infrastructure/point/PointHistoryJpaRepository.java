package com.loopers.infrastructure.point;

import com.loopers.domain.order.Order;
import com.loopers.domain.point.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
    boolean existsByUserPk(Long userPk);

    List<PointHistory> order(Order order);

    Optional<PointHistory> findByOrderId(Long orderId);
}
