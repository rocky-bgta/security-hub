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
@Document(collection = "credit_allocation_reasons")
public class CreditAllocationReason {

    @Id
    private String id;

    private String reasonName;        // e.g., "New Customer Bonus", "Loyalty Reward"
    private String description;       // Optional description
    private Boolean isActive;          // true = Active, false = Inactive (default: true)

    // System-managed fields
    private String createdBy;         // ID of the user who created this record
    private Instant createdAt;         // Timestamp when created
    private String updatedBy;          // ID of the user who last updated this record
    private Instant updatedAt;         // Timestamp when last updated
}

