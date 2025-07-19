package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_histories")
public class PointHistory extends BaseEntity {
    private String userId;
    private Long point;
    private PointHistoryType type;

    private PointHistory(String userId, Long point,  PointHistoryType pointHistoryType) {
        this.userId = userId;
        this.point = point;
        this.type = pointHistoryType;
    }

    public static PointHistory create(String userId, Long point, PointHistoryType pointHistoryType) {
        return new PointHistory(userId, point, pointHistoryType);
    }

}
