package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.dto.enums.SyncRunStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanSyncResultDto {
    private ShodanSubjectType subjectType;
    private int subjectsProcessed;
    private int alertsCreated;
    private int alertsUpdated;
    private int errorsEncountered;
    private Instant syncStartedAt;
    private Instant syncCompletedAt;
    private long durationMs;
    private SyncRunStatus status;
    private String message;

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
