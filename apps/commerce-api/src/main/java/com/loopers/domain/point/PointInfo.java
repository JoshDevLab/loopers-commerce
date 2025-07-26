package com.loopers.domain.point;

import java.math.BigDecimal;

public record PointInfo(
        Long id,
        Long userPk,
        BigDecimal pointBalance
) {

    public static PointInfo of(Point point) {
        return new PointInfo(
                point.getId(),
                point.getUserPk(),
                point.getPointBalance()
        );
    }
}
