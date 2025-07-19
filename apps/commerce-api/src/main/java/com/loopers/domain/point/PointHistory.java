package com.loopers.domain.point;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PointHistory {
    private Long id;
    private String userId;
    private Long point;
    private PointHistoryType type;
    private LocalDateTime registeredAt;

    private PointHistory(String userId, Long point,  PointHistoryType pointHistoryType, LocalDateTime registeredAt) {
        this.userId = userId;
        this.point = point;
        this.type = pointHistoryType;
        this.registeredAt = registeredAt;
    }

    public static PointHistory create(String userId, Long point, PointHistoryType pointHistoryType, LocalDateTime registeredAt) {
        return new PointHistory(userId, point, pointHistoryType, registeredAt);
    }

    public void setId(Long id) {
        this.id = id;
    }
}
