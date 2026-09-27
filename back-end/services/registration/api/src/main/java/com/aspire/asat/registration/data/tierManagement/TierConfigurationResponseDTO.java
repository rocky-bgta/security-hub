package com.aspire.asat.registration.data.tierManagement;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TierConfigurationResponseDTO {
    private String id;
    private String tierName;
    private double commissionPercentage;
    private double salesThreshold;
    private boolean active;
    private String eligibilityCriteria;
    private String tierBenefits;
    private Instant createdAt;
    private String tierDescription;
}
