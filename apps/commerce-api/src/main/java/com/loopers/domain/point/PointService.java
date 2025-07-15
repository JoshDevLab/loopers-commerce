package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {
    private final PointRepository pointRepository;

    public void initPoint(String userId) {
        Point point = Point.createInit(userId);
        pointRepository.save(point);
    }

    public Point getPoint(String userId) {
        return pointRepository.findByUserId(userId).orElse(null);
    }

    public Point chargingPoint(String userId, Long chargePoint) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.POINT_NOT_FOUND, userId + "가 가지고 있는 포인트가 없습니다."));
        point.charge(chargePoint);
        return pointRepository.save(point);
    }
}
