package com.aspire.asat.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditOperationRequestDTO {

    @NotBlank
    private String clientId;

    @NotNull
    private Double amount;

    private String referenceId;
    private String reason;
}
