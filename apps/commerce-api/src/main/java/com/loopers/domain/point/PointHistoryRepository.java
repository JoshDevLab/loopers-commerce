package com.loopers.domain.point;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);

    boolean existsByUserId(Long userPk);
}
