package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistory;
import com.loopers.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return pointHistoryJpaRepository.save(pointHistory);
    }

    @Override
    public boolean existsByUserId(Long userPk) {
        return pointHistoryJpaRepository.existsByUserPk(userPk);
    }

    @Override
    public Optional<PointHistory> findByOrderId(Long orderId) {
        return pointHistoryJpaRepository.findByOrderId(orderId);
    }

    @Override
    public void delete(PointHistory pointHistory) {
        pointHistoryJpaRepository.delete(pointHistory);
    }

    @Override
    public List<PointHistory> findAll() {
        return pointHistoryJpaRepository.findAll();
    }

}
