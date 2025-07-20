package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointService {
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void initPoint(Long userPk) {
        Point point = Point.createInit(userPk);
        pointRepository.save(point);
    }

    @Transactional(readOnly = true)
    public PointInfo getPoint(Long userPk) {
        Point point = pointRepository.findByUserPk(userPk)
                .orElseThrow(() ->
                        new CoreException(ErrorType.POINT_NOT_FOUND, "보유하고 있는 포인트가 없습니다.")
                );
        return PointInfo.of(point);
    }

    @Transactional
    public PointInfo charge(Long userPk, Long chargePoint) {
        Point point = pointRepository.findByUserPk(userPk)
                .orElseThrow(() -> new CoreException(ErrorType.POINT_NOT_FOUND, "보유하고 있는 포인트가 없습니다."));
        point.charge(chargePoint);
        pointHistoryRepository.save(PointHistory.create(point.getUserPk(), chargePoint, PointHistoryType.CHARGE));
        return PointInfo.of(point);
    }
}
