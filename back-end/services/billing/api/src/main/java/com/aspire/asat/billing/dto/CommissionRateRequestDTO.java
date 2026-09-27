package com.aspire.asat.billing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

@Data
public class CommissionRateRequestDTO {

    private String clientId;

    private String mspId;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.0", message = "Commission must be at least 30%")
    @DecimalMax(value = "50.0", message = "Commission cannot exceed 50%")
    private Double commissionPercentage;

    private Map<String, Object> eligibilityCriteria;

    @DecimalMin(value = "0.0", message = "Minimum invoice amount must be positive")
    private Double minInvoiceAmount;
}
