package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for recipient breaches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientBreachDto {

    private String id;
    private String breachRecordId;
    private String breachName;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<String> tags;
    private int breachCount;
    private RecipientBreachStatus status;
    private Instant notifiedAt;
    private Instant passwordResetAt;
    private Instant resolvedAt;
    private List<ActionLogDto> actionLogs;
    private Instant createdAt;

    // Display helpers
    private String statusLabel;
    private boolean canNotify;
    private boolean canResetPassword;
    private boolean canResolve;
}
