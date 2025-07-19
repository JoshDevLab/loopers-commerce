package com.loopers.domain.point;

import java.time.LocalDate;

public record PointHistoryInfo(
    Long id,
    String userId,
    Long point,
    PointHistoryType type,
    LocalDate createdAt
) {
    public static PointHistoryInfo of(PointHistory pointHistory) {
        return new PointHistoryInfo(
            pointHistory.getId(),
            pointHistory.getUserId(),
            pointHistory.getPoint(),
            pointHistory.getType(),
            pointHistory.getCreatedAt().toLocalDate()
        );
    }
}
