package com.aspire.asat.registration.data.netTerm;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateNetTermConfigurationRequestDTO {

    @NotBlank(message = "Net term name is required")
    @Size(max = 200, message = "Net term name must not exceed 200 characters")
    private String netTermName;

    @NotNull(message = "Net term in days is required")
    @Min(value = 1, message = "Net term in days must be at least 1")
    private Integer netTermInDays;

    @NotNull(message = "isActive status is required")
    private Boolean isActive;
}

