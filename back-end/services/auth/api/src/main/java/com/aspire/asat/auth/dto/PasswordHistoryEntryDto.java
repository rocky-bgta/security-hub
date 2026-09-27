package com.aspire.asat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a single entry in a user's password history (timestamp only; no password data is exposed).
 * Used by the frontend to show when the user last changed their password (e.g. "You cannot reuse passwords from these dates").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistoryEntryDto {
    /** When this password was set (ISO-8601). */
    private Instant changedAt;
}
