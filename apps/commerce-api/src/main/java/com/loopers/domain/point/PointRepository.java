package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Point save(Point point);

    Optional<Point> findByUserPk(Long userPk);

    Optional<Point> findByUserPkWithLock(Long userPk);
}
