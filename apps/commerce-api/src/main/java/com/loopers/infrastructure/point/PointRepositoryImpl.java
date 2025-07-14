package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PointRepositoryImpl implements PointRepository {

    private final Map<String, Point> storage = new ConcurrentHashMap<>();

    @Override
    public Point save(Point point) {
        return storage.put(point.getUserId(), point);
    }

    @Override
    public Optional<Point> findByUserId(String userId) {
        return Optional.ofNullable(storage.get(userId));
    }
}
