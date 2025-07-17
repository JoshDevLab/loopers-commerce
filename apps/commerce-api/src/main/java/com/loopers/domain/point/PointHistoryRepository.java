package com.loopers.domain.point;

public interface PointHistoryRepository {
    Long save(PointHistory pointHistory);

    void delete(Long pointHistoryId);

    PointHistory findById(Long savedId);

    boolean existsByUserId(String userId);
}
