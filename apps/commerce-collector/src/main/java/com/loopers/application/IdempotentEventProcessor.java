package com.loopers.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentEventProcessor {

    private final EventHandledRecorder recorder;

    /** 실행 자체를 단 한 번만: 선점 성공 시에만 work 실행 (중복이면 false 반환) */
    @Transactional
    public boolean processExactlyOnce(String eventId, String eventType, String aggregateId, Runnable work) {
        if (!StringUtils.hasText(eventId)) {
            log.warn("eventId가 비어있어 처리 불가");
            return false;
        }

        // 선점 삽입 시도
        boolean started = recorder.tryStart(eventId, eventType, aggregateId);
        if (!started) {
            log.debug("중복 감지(이미 실행되었거나 실행 중): eventId={}", eventId);
            return false; // 여기서 바로 스킵
        }

        try {
            work.run();
            recorder.markSuccess(eventId);
            return true;
        } catch (Exception e) {
            recorder.markFailure(eventId, e.getMessage());
            throw e;
        }
    }

}
