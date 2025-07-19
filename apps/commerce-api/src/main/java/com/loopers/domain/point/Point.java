package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "points")
public class Point extends BaseEntity {
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

    public void charge(Long chargePoint) {
        if (chargePoint <= 0) {
            throw new CoreException(ErrorType.POINT_CHARGING_ERROR, chargePoint + "는 0원 이하 이므로 충전이 불가합니다.");
        }
        this.pointBalance += chargePoint;
    }
}
