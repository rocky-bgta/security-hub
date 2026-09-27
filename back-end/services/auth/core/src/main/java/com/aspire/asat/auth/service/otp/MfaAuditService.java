package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.dto.enums.MfaAuditEventType;
import com.aspire.asat.auth.entity.MfaAuditLog;
import com.aspire.asat.auth.repository.MfaAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Persists MFA OTP security events. Never records the OTP value.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaAuditService {

    public static final String OUTCOME_SUCCESS = "SUCCESS";
    public static final String OUTCOME_DENIED = "DENIED";

    private final MfaAuditLogRepository auditLogRepository;

    public void record(MfaAuditEventType eventType,
                       UUID userId,
                       String method,
                       UUID sessionId,
                       String ipAddress,
                       String outcome,
                       String reason,
                       Map<String, Object> metadata) {
        Map<String, Object> safeMetadata = sanitize(metadata);
        try {
            MfaAuditLog entry = MfaAuditLog.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .eventType(eventType.name())
                    .method(method)
                    .sessionId(sessionId)
                    .ipAddress(ipAddress)
                    .outcome(outcome)
                    .reason(reason)
                    .metadata(safeMetadata)
                    .timestamp(Instant.now())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist MFA audit event {} for user {}: {}", eventType, userId, e.getMessage());
        }
    }

    private static Map<String, Object> sanitize(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return metadata;
        }
        if (metadata.containsKey("otp") || metadata.containsKey("code") || metadata.containsKey("codeHash")) {
            throw new IllegalArgumentException("OTP values must never be written to the audit log");
        }
        return metadata;
    }
}
