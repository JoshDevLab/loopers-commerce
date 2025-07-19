package com.loopers.domain.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public PointHistoryInfo save(String userId, Long point) {
        PointHistory pointHistory = PointHistory.create(userId, point, PointHistoryType.CHARGE);
        return PointHistoryInfo.of(pointHistoryRepository.save(pointHistory));
    }

}
