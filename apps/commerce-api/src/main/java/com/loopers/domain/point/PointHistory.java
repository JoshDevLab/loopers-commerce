package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "point_histories")
public class PointHistory extends BaseEntity {
    private Long userPk;
    private BigDecimal point;

    @Enumerated(EnumType.STRING)
    private PointHistoryType type;

    private PointHistory(Long userPk, BigDecimal point, PointHistoryType pointHistoryType) {
        this.userPk = userPk;
        this.point = point;
        this.type = pointHistoryType;
    }

    public static PointHistory create(Long userPk, BigDecimal point, PointHistoryType pointHistoryType) {
        return new PointHistory(userPk, point, pointHistoryType);
    }

}
