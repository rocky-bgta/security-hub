package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// Credit Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditResponseDTO {
    private String id;
    private String clientId;
    private Double totalCredits;
    private Double availableCredits;
    private Instant expirationDate;
    private Boolean active;
    private String reason;
    private Instant createdAt;
    private String addedBy;
}
