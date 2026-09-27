package com.aspire.asat.billing.dto.mspUser.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class CreditCreateRequestDTO {
    @NotBlank
    private String clientId;

    @Min(0)
    private Double creditAmount;

    private Instant expirationDate;
    private String reason; // Optional notes or description
    private String addedBy;
}
