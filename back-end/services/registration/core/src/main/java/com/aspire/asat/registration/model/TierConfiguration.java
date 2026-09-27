package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tier_configurations")
public class TierConfiguration {

    @Id
    private String id;

    private String tierName;             // e.g., Bronze, Silver, Gold
    private double commissionPercentage; // e.g., 10.0 for 10%
    private double salesThreshold;       // e.g., 5000.00 USD

    private boolean active;              // true = Active, false = Inactive

    private String eligibilityCriteria;  // Required criteria description

    private String tierBenefits;         // Optional benefit list

    private Instant createdAt;           // Timestamp
    private String tierDescription;
}
