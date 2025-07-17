package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistory;
import com.loopers.domain.point.PointHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, PointHistory> storage = new ConcurrentHashMap<>();

    @Override
    public Long save(PointHistory pointHistory) {
        long id = idGenerator.incrementAndGet();
        storage.put(id, pointHistory);
        pointHistory.setId(id);
        return id;
    }

    @Override
    public void delete(Long pointHistoryId) {
        storage.remove(pointHistoryId);
    }

    @Override
    public PointHistory findById(Long pointHistoryId) {
        return storage.get(pointHistoryId);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return storage.entrySet()
                .stream()
                .anyMatch(entry -> entry.getValue().getUserId().equals(userId));
    }
}
