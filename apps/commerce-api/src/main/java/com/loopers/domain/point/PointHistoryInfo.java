package com.loopers.domain.point;

import java.time.LocalDate;

public record PointHistoryInfo(
        Long id,
        Long userPk,
        Long point,
        PointHistoryType type,
        LocalDate createdAt
) {
}
