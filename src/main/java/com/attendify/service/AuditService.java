package com.attendify.service;

import com.attendify.entity.AuditLog;
import com.attendify.entity.User;
import com.attendify.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User performedBy, String action, String entityName, String entityId, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog(performedBy, action, entityName, entityId, details, ipAddress != null ? ipAddress : "127.0.0.1");
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }
}
