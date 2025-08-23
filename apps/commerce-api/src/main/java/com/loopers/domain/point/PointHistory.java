package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Order;
import jakarta.persistence.*;
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

    private Long orderId;

    private PointHistory(Long userPk, BigDecimal point, PointHistoryType pointHistoryType) {
        this.userPk = userPk;
        this.point = point;
        this.type = pointHistoryType;
    }

    public static PointHistory createChargingHistory(Long userPk, BigDecimal point, PointHistoryType pointHistoryType) {
        return new PointHistory(userPk, point, pointHistoryType);
    }

    public static PointHistory createUsingHistory(Long userPk, BigDecimal paidAmount, PointHistoryType pointHistoryType, Long orderId) {
        PointHistory pointHistory = new PointHistory();
        pointHistory.userPk = userPk;
        pointHistory.point = paidAmount;
        pointHistory.type = pointHistoryType;
        pointHistory.orderId = orderId;
        return pointHistory;
    }
}
