package com.loopers.domain.auditlog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logEvent(AuditLogCommand command) {
        auditLogRepository.save(command.toEntity());
    }
}
