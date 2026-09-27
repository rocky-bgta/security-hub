package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanMonitorDto {
    private String id;
    private ShodanSubjectType subjectType;
    private String subject;
    private boolean enabled;
    private String notes;
    private Instant lastSyncAt;
    private long openAlerts;
    private Instant createdAt;
    private Instant updatedAt;
}
