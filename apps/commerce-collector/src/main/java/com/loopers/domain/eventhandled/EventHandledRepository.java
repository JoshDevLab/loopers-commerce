package com.loopers.domain.eventhandled;

import java.util.List;
import java.util.Optional;

public interface EventHandledRepository {
    void save(EventHandled eventHandled);
    Optional<EventHandled> findByEventId(String eventId);
    List<EventHandled> findAll();
}
