package com.loopers.application;

import com.loopers.domain.eventhandled.EventHandled;
import com.loopers.domain.eventhandled.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EventHandledRecorder {

    private final EventHandledRepository eventHandledRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryStart(String eventId, String eventType, String aggregateId) {
        try {
            eventHandledRepository.save(EventHandled.inProgress(eventId, eventType, aggregateId));
            return true;
        } catch (DataIntegrityViolationException dup) {
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(String eventId) {
        eventHandledRepository.findByEventId(eventId).ifPresent(EventHandled::markSuccess);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailure(String eventId, String errorMessage) {
        eventHandledRepository.findByEventId(eventId).ifPresent(e -> e.markFailure(errorMessage));
    }
}
