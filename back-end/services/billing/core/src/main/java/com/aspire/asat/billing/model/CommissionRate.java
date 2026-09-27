package com.aspire.asat.billing.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "commission_rates")
public class CommissionRate {

    @Id
    private String id;

    private String clientId;       // References the client or organization
    private String mspId;          // Optional: Can refer to assigned user, agent, or representative

    private Double commissionPercentage; // Must be between 30 and 50

    private Map<String, Object> eligibilityCriteria;  // Flexible JSON-based conditions
    private Double minInvoiceAmount;     // Optional threshold

    private boolean active = true;

    private Instant createdAt = Instant.now();
    private Instant updatedAt;
}
