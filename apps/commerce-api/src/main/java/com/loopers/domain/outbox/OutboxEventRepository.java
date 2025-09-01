package com.loopers.domain.outbox;

public interface OutboxEventRepository {
    void save(OutboxEvent event);
}
