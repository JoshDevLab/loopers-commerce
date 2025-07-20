package com.loopers.domain.point;

public record PointInfo(
        Long id,
        Long userPk,
        Long pointBalance
) {

    public static PointInfo of(Point point) {
        return new PointInfo(
                point.getId(),
                point.getUserPk(),
                point.getPointBalance()
        );
    }
}
