package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistory;
import com.loopers.domain.point.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return pointHistoryJpaRepository.save(pointHistory);
    }

    @Override
    public void delete(Long pointHistoryId) {
        pointHistoryJpaRepository.deleteById(pointHistoryId);
    }

    @Override
    public PointHistory findById(Long savedId) {
        return pointHistoryJpaRepository.findById(savedId).orElse(null);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return pointHistoryJpaRepository.existsByUserId(userId);
    }
}
