package com.aspire.asat.registration.data.creditAllocation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCreditAllocationReasonRequestDTO {

    @NotBlank(message = "Reason name is required")
    @Size(max = 200, message = "Reason name must not exceed 200 characters")
    private String reasonName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "isActive status is required")
    private Boolean isActive;
}

