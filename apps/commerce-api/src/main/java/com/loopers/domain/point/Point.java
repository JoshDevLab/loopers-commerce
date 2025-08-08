package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "points")
public class Point extends BaseEntity {

    @Column(name = "point_balance", nullable = false)
    private BigDecimal pointBalance;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userPk;

    private Point(BigDecimal pointBalance, Long userPk) {
        this.pointBalance = pointBalance;
        this.userPk = userPk;
    }

    public static Point createInit(Long userPk) {
        return new Point(BigDecimal.ZERO, userPk);
    }

    public static Point create(BigDecimal pointBalance, Long userPk) {
        return new Point(pointBalance, userPk);
    }

    public void charge(BigDecimal chargePoint) {
        if (chargePoint.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreException(ErrorType.POINT_CHARGING_ERROR, chargePoint + "는 0원 이하 이므로 충전이 불가합니다.");
        }
        this.pointBalance = this.pointBalance.add(chargePoint);
    }

    public void use(BigDecimal paidAmount) {
        if (this.pointBalance.compareTo(paidAmount) < 0) {
            throw new CoreException(ErrorType.INSUFFICIENT_POINT, "포인트가 부족합니다. 보유 포인트: " + this.pointBalance + ", 사용 포인트: " + paidAmount);
        }
        this.pointBalance = this.pointBalance.subtract(paidAmount);
    }

    public void recovery(BigDecimal usedPoint) {
        this.pointBalance = this.pointBalance.add(usedPoint);
    }
}
