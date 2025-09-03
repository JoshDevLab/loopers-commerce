package com.loopers.infrastructure.auditlog;

import com.loopers.domain.auditlog.AuditLog;
import com.loopers.domain.auditlog.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class auditLogRepositoryImpl implements AuditLogRepository {
    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return auditLogJpaRepository.save(auditLog);
    }
}
