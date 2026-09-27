package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.SyncRunStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private SyncRunStatus status;
    private String message;
}
