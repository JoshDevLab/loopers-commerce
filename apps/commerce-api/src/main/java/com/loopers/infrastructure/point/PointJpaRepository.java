package com.loopers.infrastructure.point;

import com.loopers.domain.point.Point;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByUserPk(Long userPk);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Point p where p.userPk = :userPk")
    Optional<Point> findByUserPkWithLock(Long userPk);
}
