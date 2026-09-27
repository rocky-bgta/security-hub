package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for action logs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLogDto {

    private String action;
    private String actionLabel;
    private String performedBy;
    private Instant performedAt;
    private String notes;
}
