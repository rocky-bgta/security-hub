package com.aspire.asat.cms.dto.subPackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrialSubPackageCreationRequestDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Package ID is required")
    private String packageId;

    @NotBlank(message = "Product Package ID is required")
    private String productPackageId;

    @NotBlank(message = "Client Admin ID is required")
    private String clientAdminId;

    @NotNull(message = "Trial period days is required")
    @Min(value = 1, message = "Trial period must be at least 1 day")
    private Integer trialPeriodDays;
}

