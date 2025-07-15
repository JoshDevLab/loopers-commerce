package com.loopers.domain.point;

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

    public void chargingPoint(String userId, Long chargePoint) {
        pointRepository.findByUserId(userId).ifPresent(point -> point.charge(chargePoint));
    }
}
