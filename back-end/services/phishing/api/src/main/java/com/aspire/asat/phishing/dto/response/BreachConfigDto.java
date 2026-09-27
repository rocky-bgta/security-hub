package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for breach detection configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachConfigDto {

    private String id;
    private boolean collectBreachData;
    private List<String> monitoredDomains;
    private boolean autoNotifyUsers;
    private boolean requirePasswordReset;
    private int syncIntervalHours;
    private Instant lastSyncAt;
    private Instant updatedAt;
    private String updatedBy;
}
