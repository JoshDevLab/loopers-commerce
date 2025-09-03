package com.loopers.domain.eventhandled;

import java.util.Optional;

public interface EventHandledRepository {
    void save(EventHandled eventHandled);
    Optional<EventHandled> findByEventId(String eventId);
}
