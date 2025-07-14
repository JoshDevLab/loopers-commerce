package com.loopers.domain.point;

import lombok.Getter;

@Getter
public class Point {
    private Long pointBalance;
    private String userId;

    private Point(Long pointBalance, String userId) {
        this.pointBalance = pointBalance;
        this.userId = userId;
    }

    public static Point createInit(String userId) {
        return new Point(0L, userId);
    }

    public static Point create(Long pointBalance, String userId) {
        return new Point(pointBalance, userId);
    }
}
