package com.aspire.asat.registration.data.tierManagement;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTierConfigurationRequestDTO {

    @NotBlank(message = "Tier name is required")
    private String tierName;

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission must be >= 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Commission must be <= 100")
    private double commissionPercentage;

    @Min(value = 0, message = "Sales threshold must be non-negative")
    private double salesThreshold;

    @NotNull(message = "Status is required")
    private Boolean active;

    @NotBlank(message = "Eligibility criteria is required")
    private String eligibilityCriteria;

    private String tierBenefits;

    @Size(max = 500, message = "Tier description must not exceed 500 characters")
    private String tierDescription;
}
