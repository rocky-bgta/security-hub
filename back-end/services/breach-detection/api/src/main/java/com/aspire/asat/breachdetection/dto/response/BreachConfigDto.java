package com.aspire.asat.breachdetection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachConfigDto {
    private String id;
    private boolean collectBreachData;
    private String email;
    private List<String> monitoredDomains;
    private boolean autoNotifyUsers;
    private boolean requirePasswordReset;
    private int syncIntervalHours;
    private Instant lastSyncAt;
    private Instant updatedAt;
    private String updatedBy;
}
