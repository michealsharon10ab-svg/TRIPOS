package com.bughunters.pos.administration.service;

import com.bughunters.pos.administration.entity.AuditLog;
import com.bughunters.pos.administration.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(Long tenantId, Long branchId, Long userId, String username,
                    String action, String details, HttpServletRequest request) {
        try {
            String ipAddress = "0.0.0.0";
            String userAgent = "Unknown";

            if (request != null) {
                String xForwarded = request.getHeader("X-Forwarded-For");
                if (xForwarded != null && !xForwarded.isBlank()) {
                    ipAddress = xForwarded.split(",")[0].trim();
                } else {
                    ipAddress = request.getRemoteAddr();
                }
                userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 250) {
                    userAgent = userAgent.substring(0, 250);
                }
            }

            AuditLog log = new AuditLog(tenantId, branchId, userId, username, action, details, ipAddress, userAgent);
            auditLogRepository.save(log);
            logger.info("AUDIT_EVENT: action={}, user={}, tenantId={}, branchId={}, details={}",
                    action, username, tenantId, branchId, details);
        } catch (Exception e) {
            logger.error("Failed to persist security audit log for action: {}", action, e);
        }
    }
}
