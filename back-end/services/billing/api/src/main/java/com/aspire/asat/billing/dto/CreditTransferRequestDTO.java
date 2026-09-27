package com.aspire.asat.billing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreditTransferRequestDTO {

    @NotBlank(message = "From Client ID cannot be blank")
    private String fromClientId;

    @NotBlank(message = "To Client ID cannot be blank")
    private String toClientId;

    @Min(1)
    private Double amount;

    private String reason;
    private String initiatedBy; // optional
}
