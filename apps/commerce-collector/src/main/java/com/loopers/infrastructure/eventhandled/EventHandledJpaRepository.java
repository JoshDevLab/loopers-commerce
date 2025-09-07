package com.loopers.infrastructure.eventhandled;

import com.loopers.domain.eventhandled.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {
    Optional<EventHandled> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
    long countByEventId(String eventId);
}
