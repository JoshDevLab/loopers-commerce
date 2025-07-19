package com.loopers.domain.point;

public record PointInfo(
        Long id,
        String userId,
        Long pointBalance
) {

    public static PointInfo of(Point point) {
        return new PointInfo(
                point.getId(),
                point.getUserId(),
                point.getPointBalance()
        );
    }
}
