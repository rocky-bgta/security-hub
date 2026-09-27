package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for breach sync operation results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachSyncResultDto {

    private int domainsProcessed;
    private int newBreachesFound;
    private int newRecipientsFound;
    private int errorsEncountered;
    private Instant syncStartedAt;
    private Instant syncCompletedAt;
    private long durationMs;
    private String status;      // SUCCESS, PARTIAL, FAILED
    private String message;
}
