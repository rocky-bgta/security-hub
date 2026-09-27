package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Durable MFA OTP security audit trail. Never stores the OTP value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "mfa_audit_logs")
@CompoundIndex(name = "mfa_audit_user_time_idx", def = "{'userId': 1, 'timestamp': -1}")
public class MfaAuditLog {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

    private String eventType;

    private String method;

    private UUID sessionId;

    private String ipAddress;

    private String outcome;

    private String reason;

    private Map<String, Object> metadata;

    @CreatedDate
    private Instant timestamp;
}
