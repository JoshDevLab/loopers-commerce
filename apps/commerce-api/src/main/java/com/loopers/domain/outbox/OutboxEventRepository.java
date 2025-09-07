package com.loopers.domain.outbox;

import java.time.ZonedDateTime;
import java.util.List;

public interface OutboxEventRepository {
    void save(OutboxEvent event);
    List<OutboxEvent> findUnpublishedEvents(int limit);
    void markAsPublished(String eventId);
    Long deleteOldPublishedEvents(ZonedDateTime threshold);
}
