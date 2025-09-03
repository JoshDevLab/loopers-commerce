package com.loopers.infrastructure.eventhandled;

import com.loopers.domain.eventhandled.EventHandled;
import com.loopers.domain.eventhandled.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class EventHandledRepositoryImpl implements EventHandledRepository {
    private final EventHandledJpaRepository eventHandledJpaRepository;

    @Override
    public void save(EventHandled eventHandled) {
        eventHandledJpaRepository.save(eventHandled);
    }

    @Override
    public Optional<EventHandled> findByEventId(String eventId) {
        return eventHandledJpaRepository.findByEventId(eventId);
    }
}
