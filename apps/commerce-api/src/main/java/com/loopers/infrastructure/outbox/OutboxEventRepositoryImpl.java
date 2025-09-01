package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepository {
    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public void save(OutboxEvent event) {
        outboxEventJpaRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findUnpublishedEvents(int limit) {
        return outboxEventJpaRepository.findUnpublishedEventsOrderByCreatedAt(
                PageRequest.of(0, limit)
        );
    }

    @Override
    @Transactional
    public void markAsPublished(String eventId) {
        outboxEventJpaRepository.markAsPublished(eventId, ZonedDateTime.now());
    }

    @Override
    @Transactional
    public Long deleteOldPublishedEvents(ZonedDateTime threshold) {
        return outboxEventJpaRepository.deleteByPublishedTrueAndPublishedAtBefore(threshold);
    }
}
