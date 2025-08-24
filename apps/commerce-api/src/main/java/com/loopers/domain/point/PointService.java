package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    public Point getPoint(Long userPk) {
        return pointRepository.findByUserPk(userPk)
                .orElseThrow(() ->
                        new CoreException(ErrorType.POINT_NOT_FOUND, "보유하고 있는 포인트가 없습니다.")
                );
    }

    @Transactional
    public Point charge(Long userPk, BigDecimal chargePoint) {
        Point point = pointRepository.findByUserPkWithLock(userPk)
                .orElseThrow(() -> new CoreException(ErrorType.POINT_NOT_FOUND, "보유하고 있는 포인트가 없습니다."));
        point.charge(chargePoint);
        pointHistoryRepository.save(PointHistory.createChargingHistory(point.getUserPk(), chargePoint, PointHistoryType.CHARGE));
        return point;
    }

    @Transactional
    public Point use(Long userPk, BigDecimal paidAmount, Long orderId) {
        Point point = pointRepository.findByUserPkWithLock(userPk)
                .orElseThrow(() -> new CoreException(ErrorType.POINT_NOT_FOUND, "보유하고 있는 포인트가 없습니다."));
        point.use(paidAmount);
        pointHistoryRepository.save(PointHistory.createUsingHistory(point.getUserPk(), paidAmount, PointHistoryType.USE, orderId));
        return point;
    }

    public void recovery(Long orderId) {
        PointHistory pointHistory = pointHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.POINT_HISTORY_NOT_FOUND));

        Point point = pointRepository.findByUserPk(pointHistory.getUserPk())
                .orElseThrow(() -> new CoreException(ErrorType.POINT_NOT_FOUND));

        point.recovery(pointHistory.getPoint());

        pointHistoryRepository.delete(pointHistory);
    }
}
