package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Embedded model for action logs in breach records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLog {

    private String action;          // NOTIFY, PASSWORD_RESET, MFA_ENABLED, RESOLVED

    private String performedBy;     // User who performed the action

    private Instant performedAt;

    private String notes;           // Additional details
}
