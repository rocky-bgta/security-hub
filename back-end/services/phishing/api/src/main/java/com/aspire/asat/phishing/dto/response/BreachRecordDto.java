package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import com.aspire.asat.phishing.dto.enums.BreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for breach records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachRecordDto {

    private String id;
    private String domain;
    private String breachName;
    private Instant dateOfBreach;
    private String description;
    private List<String> compromisedDataTypes;
    private int recipientCount;
    private BreachSeverity severity;
    private BreachStatus status;
    private String sourceApi;
    private String externalBreachId;
    private Instant createdAt;
    private Instant updatedAt;

    // Display helpers
    private String severityLabel;
    private String statusLabel;
    private String compromisedDataSummary;
}
