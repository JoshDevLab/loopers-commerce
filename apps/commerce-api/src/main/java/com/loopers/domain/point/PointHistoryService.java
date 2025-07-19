package com.loopers.domain.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    public Long save(String userId, Long point) {
        PointHistory pointHistory = PointHistory.create(userId, point, PointHistoryType.CHARGE);
        return pointHistoryRepository.save(pointHistory);
    }

    public void delete(Long pointHistoryId) {
        pointHistoryRepository.delete(pointHistoryId);
    }
}
