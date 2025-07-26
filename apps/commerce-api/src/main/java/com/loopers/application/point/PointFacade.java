package com.loopers.application.point;

import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;

    public PointInfo getPoint(Long userPk) {
        return PointInfo.of(pointService.getPoint(userPk));
    }

    public PointInfo charge(Long userPk, BigDecimal chargePoint) {
        return PointInfo.of(pointService.charge(userPk, chargePoint));
    }

}
