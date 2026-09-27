package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB entity for breach detection configuration.
 * Stores client-specific settings for breach monitoring.
 */
@Document(collection = "breach_detection_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachDetectionConfig {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientId;

    @Builder.Default
    private boolean collectBreachData = true;

    @Builder.Default
    private List<String> monitoredDomains = new ArrayList<>();

    @Builder.Default
    private boolean autoNotifyUsers = false;

    @Builder.Default
    private boolean requirePasswordReset = false;

    @Builder.Default
    private int syncIntervalHours = 6;

    private Instant lastSyncAt;

    @LastModifiedDate
    private Instant updatedAt;

    @LastModifiedBy
    private String updatedBy;
}
