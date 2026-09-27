package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for breach detection configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachConfigRequest {

    private boolean collectBreachData;

    private List<String> monitoredDomains;

    private boolean autoNotifyUsers;

    private boolean requirePasswordReset;

    @Min(value = 1, message = "Sync interval must be at least 1 hour")
    @Max(value = 24, message = "Sync interval cannot exceed 24 hours")
    private int syncIntervalHours;
}
