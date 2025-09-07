package com.loopers.infrastructure.outbox;

import com.loopers.domain.outbox.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 미발행 이벤트 조회 (생성시간 순)
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false ORDER BY o.createdAt ASC")
    List<OutboxEvent> findUnpublishedEventsOrderByCreatedAt(Pageable pageable);

    /**
     * 이벤트를 발행 완료로 마킹
     */
    @Modifying
    @Query("UPDATE OutboxEvent o SET o.published = true, o.publishedAt = :publishedAt WHERE o.eventId = :eventId")
    void markAsPublished(@Param("eventId") String eventId, @Param("publishedAt") ZonedDateTime publishedAt);

    /**
     * 오래된 발행 완료 이벤트 삭제
     */
    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.published = true AND o.publishedAt < :threshold")
    Long deleteByPublishedTrueAndPublishedAtBefore(@Param("threshold") ZonedDateTime threshold);
}
